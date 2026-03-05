"""OpenAI LLM 서비스"""
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
    """OpenAI API 호출 서비스"""

    BASE_URL = "https://api.openai.com/v1/chat/completions"
    TIMEOUT = 60

    def __init__(self, db: Session):
        self.db = db
        self.api_key = settings.openai_api_key
        self.model = settings.openai_model
        self.max_tokens = settings.openai_max_tokens
        self.temperature = settings.openai_temperature
        self.client = httpx.AsyncClient(
            headers={
                "Authorization": f"Bearer {self.api_key}",
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
        OpenAI API 호출

        Args:
            system_prompt: 시스템 프롬프트 (역할/형식 정의)
            user_message: 사용자 메시지 (분석할 내용)

        Returns:
            LLM 응답 텍스트 (None if failed)
        """
        if not self.is_configured():
            logger.warning("OpenAI API 키가 설정되지 않았습니다.")
            return None

        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_message}
            ],
            "max_tokens": self.max_tokens,
            "temperature": self.temperature,
            "response_format": {"type": "json_object"}  # JSON 응답 강제
        }

        try:
            response = await self.client.post(self.BASE_URL, json=payload)
            response.raise_for_status()
            data = response.json()

            content = data["choices"][0]["message"]["content"]
            logger.debug(f"LLM 응답: {content[:200]}...")
            return content

        except httpx.HTTPStatusError as e:
            logger.error(f"OpenAI API 오류 [{e.response.status_code}]: {e.response.text}")
            return None
        except Exception as e:
            logger.error(f"LLM 호출 실패: {e}")
            return None

    async def call_json(self, system_prompt: str, user_message: str) -> Optional[dict]:
        """
        OpenAI API 호출 후 JSON 파싱

        Returns:
            파싱된 JSON dict (None if failed)
        """
        response = await self.call(system_prompt, user_message)
        if not response:
            return None

        try:
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
