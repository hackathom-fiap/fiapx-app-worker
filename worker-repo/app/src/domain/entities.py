from enum import Enum
from dataclasses import dataclass
from typing import Optional

class VideoStatus(str, Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    ERROR = "error"

@dataclass
class Video:
    id: int
    filename: str
    status: VideoStatus = VideoStatus.PENDING
    zip_path: Optional[str] = None
