import { apiClient, type ApiEnvelope } from './client';
import { normalizeUploadUri } from '../utils/uploadUri';

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

  const response = await apiClient.post<ApiEnvelope<MediaUploadResult>>('/media/upload', formData);
  return response.data.data;
}
