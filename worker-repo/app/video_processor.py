import cv2
import os
import zipfile
import shutil

def process_video_file(video_path, output_dir):
    """
    Extrai frames do vídeo (1 frame por segundo) e cria um ZIP.
    Retorna o caminho do arquivo ZIP criado.
    """
    # Criar diretório temporário para frames
    video_name = os.path.basename(video_path)
    base_name = os.path.splitext(video_name)[0]
    frames_dir = os.path.join(output_dir, "temp_frames_" + base_name)
    
    if os.path.exists(frames_dir):
        shutil.rmtree(frames_dir)
    os.makedirs(frames_dir)

    print(f"🎬 Iniciando processamento do vídeo: {video_path}")
    
    # Captura de vídeo usando OpenCV
    cap = cv2.VideoCapture(video_path)
    fps = cap.get(cv2.CAP_PROP_FPS)
    if fps == 0:
        fps = 30 # Fallback se não conseguir detectar
        
    frame_interval = int(fps) # 1 frame por segundo
    
    count = 0
    frame_count = 0
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
        frame_count += 1
    
    cap.release()
    print(f"📸 Extraídos {saved_count} frames.")

    # Criar ZIP
    zip_filename = f"{base_name}.zip"
    zip_path = os.path.join(output_dir, zip_filename)
    
    with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
        for root, dirs, files in os.walk(frames_dir):
            for file in files:
                file_path = os.path.join(root, file)
                zipf.write(file_path, os.path.basename(file_path))
    
    # Limpeza
    shutil.rmtree(frames_dir)
    print(f"✅ ZIP criado com sucesso: {zip_path}")
    
    return zip_filename
