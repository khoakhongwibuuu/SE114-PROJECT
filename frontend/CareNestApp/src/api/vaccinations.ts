import { apiGetCached, apiPost, invalidateApiGetCache } from './client';

export interface VaccinationTrackerGroup {
  stageLabel: string;
  description: string;
  vaccinations: Array<{
    id: number;
    profileId: number;
    fullName: string;
    vaccineName: string;
    doseNumber: number;
    dateGiven?: string | null;
    plannedDate?: string | null;
    clinicName?: string | null;
    status: string;
  }>;
}

export async function getVaccinationTracker(profileId: number): Promise<VaccinationTrackerGroup[]> {
  return apiGetCached<VaccinationTrackerGroup[]>(`/vaccinations/profiles/${profileId}`, undefined, {
    ttlMs: 20000,
  });
}

export async function createVaccination(profileId: number, payload: {
  vaccineName: string;
  doseNumber: number;
  dateGiven?: string | null;
  plannedDate?: string | null;
  clinicName?: string;
}): Promise<void> {
  await apiPost(`/vaccinations/profiles/${profileId}`, payload);
  invalidateApiGetCache(['/vaccinations/profiles/', '/dashboard', '/notifications']);
}

