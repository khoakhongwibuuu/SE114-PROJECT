import axios from 'axios';
import { apiClient, apiGet, apiPatch, apiPost, invalidateApiGetCache, type ApiEnvelope } from './client';

export type VerificationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface DoctorVerification {
  id: number;
  userId: number;
  userEmail?: string | null;
  userFullName?: string | null;
  certificationNumber: string;
  specialty: string;
  hospitalName: string;
  documentUrl: string;
  status: VerificationStatus;
  rejectionReason?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SubmitDoctorVerificationPayload {
  certificationNumber: string;
  specialty: string;
  hospitalName: string;
  documentUrl: string;
}

export async function submitVerification(
  payload: SubmitDoctorVerificationPayload,
): Promise<DoctorVerification> {
  const verification = await apiPost<DoctorVerification, SubmitDoctorVerificationPayload>(
    '/doctor-verifications',
    payload,
  );
  invalidateApiGetCache(['/doctor-verifications']);
  return verification;
}

export async function getMyVerificationStatus(): Promise<DoctorVerification | null> {
  try {
    const response = await apiClient.get<ApiEnvelope<DoctorVerification>>('/doctor-verifications/me');
    return response.data.data;
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      return null;
    }
    throw error instanceof Error ? error : new Error('Could not load doctor verification');
  }
}

export async function getPendingVerifications(): Promise<DoctorVerification[]> {
  return apiGet<DoctorVerification[]>('/admin/doctor-verifications/pending');
}

export async function approveVerification(id: number): Promise<DoctorVerification> {
  const verification = await apiPatch<DoctorVerification>(`/admin/doctor-verifications/${id}/approve`);
  invalidateApiGetCache(['/admin/doctor-verifications', '/auth/me']);
  return verification;
}

export async function rejectVerification(id: number, rejectionReason: string): Promise<DoctorVerification> {
  const verification = await apiPatch<DoctorVerification, { rejectionReason: string }>(
    `/admin/doctor-verifications/${id}/reject`,
    { rejectionReason },
  );
  invalidateApiGetCache(['/admin/doctor-verifications']);
  return verification;
}
