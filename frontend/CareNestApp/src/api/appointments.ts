import { apiGetCached, apiPut, apiPost, invalidateApiGetCache } from './client';

export interface AppointmentOverview {
  upcomingCount: number;
  upcomingAppointments: Array<{
    id: number;
    title: string;
    doctorName: string;
    appointmentDate: string;
    location?: string | null;
    status: string;
    dayOfWeek?: string;
    dayOfMonth?: number;
  }>;
  appointmentHistory: Array<{
    id: number;
    title: string;
    appointmentDate: string;
    displayDate: string;
    status: string;
  }>;
}

interface RawAppointmentResponse {
  id: number;
  healthProfileId: number;
  doctorName: string;
  hospitalName: string;
  address: string;
  appointmentDate: string;
  status: string;
  notes: string;
  resultNotes: string;
  createdAt: string;
  updatedAt: string;
}

export async function getAppointmentOverview(profileId: number): Promise<AppointmentOverview> {
  const allAppointments = await apiGetCached<RawAppointmentResponse[]>(`/health-profiles/${profileId}/appointments`, undefined, {
    ttlMs: 20000,
  });

  const now = new Date();
  
  const upcoming = allAppointments
    .filter(a => new Date(a.appointmentDate) >= now && a.status !== 'CANCELLED')
    .sort((a, b) => new Date(a.appointmentDate).getTime() - new Date(b.appointmentDate).getTime());
    
  const history = allAppointments
    .filter(a => new Date(a.appointmentDate) < now || a.status === 'CANCELLED')
    .sort((a, b) => new Date(b.appointmentDate).getTime() - new Date(a.appointmentDate).getTime());

  return {
    upcomingCount: upcoming.length,
    upcomingAppointments: upcoming.map(a => {
      const d = new Date(a.appointmentDate);
      return {
        id: a.id,
        title: a.hospitalName || 'Khám bệnh',
        doctorName: a.doctorName,
        appointmentDate: a.appointmentDate,
        location: a.address,
        status: a.status,
        dayOfWeek: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'][d.getDay()],
        dayOfMonth: d.getDate(),
      };
    }),
    appointmentHistory: history.map(a => {
      const d = new Date(a.appointmentDate);
      return {
        id: a.id,
        title: a.hospitalName || 'Khám bệnh',
        appointmentDate: a.appointmentDate,
        displayDate: `${d.getDate()}/${d.getMonth() + 1}/${d.getFullYear()}`,
        status: a.status,
      };
    })
  };
}

export async function createAppointment(payload: {
  healthProfileId: number;
  hospitalName: string;
  doctorName: string;
  appointmentDate: string;
  address?: string;
  notes?: string;
}): Promise<void> {
  await apiPost('/appointments', {
    healthProfileId: payload.healthProfileId,
    hospitalName: payload.hospitalName,
    doctorName: payload.doctorName,
    appointmentDate: new Date(payload.appointmentDate).toISOString(),
    address: payload.address,
    notes: payload.notes,
  });
  invalidateApiGetCache([`/health-profiles/${payload.healthProfileId}/appointments`, '/dashboard', '/notifications']);
}

export async function cancelAppointment(appointmentId: number): Promise<void> {
  await apiPut(`/appointments/${appointmentId}/cancel`);
  invalidateApiGetCache(['/health-profiles/', '/dashboard', '/notifications']);
}
