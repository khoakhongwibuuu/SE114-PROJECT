import { normalizeUploadUri } from '../utils/uploadUri';
import { API_BASE_URL } from './config';
import { getStoredSession } from './storage';

export interface MediaUploadResult {
  fileName: string;
  contentType: string;
  size: number;
  url: string;
}

export async function uploadMedia(
  fileUri: string,
  fileName: string,
  mimeType: string,
  category: string,
): Promise<MediaUploadResult> {
  const formData = new FormData();
  formData.append('file', {
    uri: normalizeUploadUri(fileUri),
    name: fileName,
    type: mimeType,
  } as unknown as Blob);
  formData.append('category', category);

  const session = await getStoredSession();
  
  const response = await fetch(`${API_BASE_URL}/media/upload`, {
    method: 'POST',
    body: formData,
    headers: {
      ...(session?.token ? { Authorization: `Bearer ${session.token}` } : {}),
    },
  });

  if (!response.ok) {
    let errorMessage = 'Đã có lỗi xảy ra khi tải ảnh lên';
    try {
      const errorData = await response.json();
      errorMessage = errorData.message || errorMessage;
    } catch {}
    throw new Error(errorMessage);
  }

  const json = await response.json();
  return json.data;
}
