import logging
from google import genai
from ai.config import GEMINI_API_KEY, MODEL_NAME

logger = logging.getLogger(__name__)

if not GEMINI_API_KEY:
    raise ValueError("GEMINI_API_KEY is not set! Please check your .env file.")

client = genai.Client(api_key=GEMINI_API_KEY)

def generate(prompt: str) -> str:
    """Generate  content using Gemini API """
    try:
        response = client.models.generate_content(
            model=MODEL_NAME,
            contents=[prompt]
        )
        return response.text
    except Exception as e:
        logger.error(f"Error generating content: {str(e)}")
        raise