from src.use_cases.process_video import ProcessVideoUseCase
from src.domain.entities import Video, VideoStatus
from unittest.mock import MagicMock

def test_process_video_success(tmp_path):
    # Arrange
    video_id = 1
    filename = "test.mp4"
    uploads_dir = tmp_path / "uploads"
    outputs_dir = tmp_path / "outputs"
    uploads_dir.mkdir()
    outputs_dir.mkdir()
    
    # Criar arquivo de vídeo falso
    video_file = uploads_dir / filename
    video_file.write_text("dummy video content")
    
    mock_repo = MagicMock()
    mock_video = Video(id=video_id, filename=filename, status=VideoStatus.PENDING)
    mock_repo.get_by_id.return_value = mock_video
    
    mock_processor = MagicMock()
    mock_processor.extract_frames_to_zip.return_value = "test.zip"
    
    mock_notifier = MagicMock()

    # Mock Prometheus metrics
    mock_videos_processed = MagicMock()
    mock_video_processing_errors = MagicMock()
    mock_video_processing_duration = MagicMock()
    
    use_case = ProcessVideoUseCase(
        mock_repo, mock_processor, mock_notifier, 
        str(uploads_dir), str(outputs_dir),
        mock_videos_processed, mock_video_processing_errors, mock_video_processing_duration
    )
    
    # Act
    use_case.execute(video_id, filename)
    
    # Assert
    assert mock_video.status == VideoStatus.COMPLETED
    assert mock_video.zip_path == "test.zip"
    assert mock_repo.update_status.call_count == 2 # 1 for PROCESSING, 1 for COMPLETED
    mock_processor.extract_frames_to_zip.assert_called_once()
    mock_videos_processed.labels.assert_any_call(status='processing')
    mock_videos_processed.labels.assert_any_call(status='completed')
    mock_video_processing_duration.observe.assert_called_once()
