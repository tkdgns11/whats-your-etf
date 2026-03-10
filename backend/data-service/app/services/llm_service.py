"""SSAFY GMS LLM 서비스

SSAFY GMS (Gen AI Management System)를 통해 Anthropic Claude API 호출
"""
import json
import logging
import httpx
from typing import Optional, Any
from sqlalchemy.orm import Session

from app.config import get_settings
from app.models.ai_prompt import AIPrompt

logger = logging.getLogger(__name__)
settings = get_settings()


class LLMService:
    """Anthropic Claude API 호출 서비스 (직접 또는 GMS 경유)"""

    TIMEOUT = 60

    def __init__(self, db: Session, use_light_model: bool = False):
        """
        Args:
            db: DB 세션
            use_light_model: True면 Haiku(저렴), False면 Sonnet(고성능)
        """
        self.db = db
        self.model = settings.gms_model_light if use_light_model else settings.gms_model
        self.max_tokens = settings.gms_max_tokens
        self.temperature = settings.gms_temperature

        # Anthropic API 직접 호출 우선, 없으면 GMS 사용
        if settings.anthropic_api_key:
            self.api_key = settings.anthropic_api_key
            self.base_url = "https://api.anthropic.com"
            logger.info("Anthropic API 직접 호출 모드")
        else:
            self.api_key = settings.gms_api_key
            self.base_url = settings.gms_base_url
            logger.info("GMS API 경유 모드")

        self.client = httpx.AsyncClient(
            headers={
                "x-api-key": self.api_key,
                "anthropic-version": "2023-06-01",
                "Content-Type": "application/json"
            },
            timeout=self.TIMEOUT
        )

    def is_configured(self) -> bool:
        """API 키가 설정되어 있는지 확인"""
        return bool(self.api_key)

    async def close(self):
        await self.client.aclose()

    def get_prompt(self, name: str) -> Optional[AIPrompt]:
        """활성화된 프롬프트 조회"""
        return self.db.query(AIPrompt).filter(
            AIPrompt.name == name,
            AIPrompt.is_active == True
        ).first()

    async def call(self, system_prompt: str, user_message: str) -> Optional[str]:
        """
        Anthropic Claude API 호출

        Args:
            system_prompt: 시스템 프롬프트 (역할/형식 정의)
            user_message: 사용자 메시지 (분석할 내용)

        Returns:
            LLM 응답 텍스트 (None if failed)
        """
        if not self.is_configured():
            logger.warning("GMS API 키가 설정되지 않았습니다.")
            return None

        # JSON 응답 형식 사용 시 메시지에 'json' 단어 필요
        json_instruction = "\n\nRespond in JSON format only. Do not include any text outside of the JSON object."

        payload = {
            "model": self.model,
            "max_tokens": self.max_tokens,
            "system": system_prompt + json_instruction,
            "messages": [
                {"role": "user", "content": user_message}
            ]
        }

        url = f"{self.base_url}/v1/messages"

        try:
            response = await self.client.post(url, json=payload)
            response.raise_for_status()
            data = response.json()

            # Anthropic 응답 형식: content[0].text
            content = data.get("content", [])
            if content and len(content) > 0:
                text = content[0].get("text", "")
                logger.debug(f"LLM 응답: {text[:200]}...")
                return text

            logger.error("LLM 응답에 content가 없습니다.")
            return None

        except httpx.HTTPStatusError as e:
            logger.error(f"Anthropic API 오류 [{e.response.status_code}]: {e.response.text}")
            return None
        except Exception as e:
            logger.error(f"LLM 호출 실패: {e}")
            return None

    async def call_json(self, system_prompt: str, user_message: str) -> Optional[dict]:
        """
        Anthropic API 호출 후 JSON 파싱

        Returns:
            파싱된 JSON dict (None if failed)
        """
        response = await self.call(system_prompt, user_message)
        if not response:
            return None

        try:
            # JSON 응답에서 ```json 블록 추출 (있는 경우)
            if "```json" in response:
                start = response.find("```json") + 7
                end = response.find("```", start)
                response = response[start:end].strip()
            elif "```" in response:
                start = response.find("```") + 3
                end = response.find("```", start)
                response = response[start:end].strip()

            return json.loads(response)
        except json.JSONDecodeError as e:
            logger.error(f"JSON 파싱 실패: {e}")
            logger.debug(f"원본 응답: {response}")
            return None

    async def analyze_with_prompt(self, prompt_name: str, user_message: str) -> Optional[dict]:
        """
        DB에서 프롬프트를 불러와 분석 수행

        Args:
            prompt_name: 프롬프트 이름 (news_analysis, portfolio_feedback 등)
            user_message: 분석할 내용

        Returns:
            분석 결과 dict (None if failed)
        """
        prompt = self.get_prompt(prompt_name)
        if not prompt:
            logger.error(f"프롬프트를 찾을 수 없습니다: {prompt_name}")
            return None

        return await self.call_json(prompt.prompt_template, user_message)
