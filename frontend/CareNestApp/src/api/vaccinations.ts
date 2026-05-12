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
  const records = await apiGetCached<any[]>(`/health-profiles/${profileId}/vaccinations`, undefined, {
    ttlMs: 20000,
  });

  // Transform backend list to UI groups
  // We group by vaccineName
  return records.map(record => ({
    stageLabel: record.vaccineName,
    description: `Tổng số ${record.totalDoses} mũi`,
    vaccinations: (record.doses || []).map((dose: any) => ({
      id: dose.id,
      profileId: record.healthProfileId,
      fullName: '', // Not needed for display here
      vaccineName: record.vaccineName,
      doseNumber: dose.doseNumber,
      dateGiven: dose.dateAdministered,
      plannedDate: dose.scheduledDate,
      clinicName: dose.location,
      status: dose.status === 'COMPLETED' ? 'DONE' : 'PENDING',
    })),
  }));
}

export async function createVaccination(profileId: number, payload: {
  vaccineName: string;
  totalDoses: number;
  startDate: string;
  doseIntervalDays?: number;
  location?: string;
  notes?: string;
}): Promise<void> {
  await apiPost(`/health-profiles/${profileId}/vaccinations`, payload);
  invalidateApiGetCache([`/health-profiles/${profileId}/vaccinations`, '/dashboard', '/notifications']);
}

