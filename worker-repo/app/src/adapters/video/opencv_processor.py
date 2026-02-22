import cv2
import os
import zipfile
import shutil
from src.ports.interfaces import VideoProcessor

class OpenCVVideoProcessor(VideoProcessor):
    def extract_frames_to_zip(self, input_path: str, output_dir: str) -> str:
        video_name = os.path.basename(input_path)
        base_name = os.path.splitext(video_name)[0]
        frames_dir = os.path.join(output_dir, "temp_frames_" + base_name)
        
        if os.path.exists(frames_dir):
            shutil.rmtree(frames_dir)
        os.makedirs(frames_dir)

        cap = cv2.VideoCapture(input_path)
        if not cap.isOpened():
            print(f"ERROR: Could not open video file {input_path}")
            raise IOError(f"Could not open video file {input_path}")
        fps = cap.get(cv2.CAP_PROP_FPS) or 30
        frame_interval = int(fps)
        
        count = 0
        saved_count = 0
        
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                break
            if count % frame_interval == 0:
                frame_name = os.path.join(frames_dir, f"frame_{saved_count:04d}.png")
                cv2.imwrite(frame_name, frame)
                saved_count += 1
            count += 1
        
        cap.release()

        zip_filename = f"{base_name}.zip"
        zip_path = os.path.join(output_dir, zip_filename)
        
        with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
            for root, dirs, files in os.walk(frames_dir):
                for file in files:
                    file_path = os.path.join(root, file)
                    zipf.write(file_path, os.path.basename(file_path))
        
        shutil.rmtree(frames_dir)
        return zip_filename
