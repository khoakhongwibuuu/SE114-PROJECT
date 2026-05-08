export const mockAuthUser = {
  id: 1,
  email: 'mockuser@example.com',
  fullName: 'Nguyễn Văn Mock',
  phone: '0123456789',
  dateOfBirth: '1990-01-01',
  gender: 'MALE',
  avatarUrl: null,
  role: 'USER',
  isVerified: true
};

export const mockFamilyDetail = {
  id: 1,
  name: 'Gia đình Mock',
  ownerId: 1,
  createdAt: '2026-01-01T00:00:00Z',
  members: [
    {
      id: 1,
      user: mockAuthUser,
      fullName: mockAuthUser.fullName,
      avatarUrl: null,
      role: 'OWNER',
      joinedAt: '2026-01-01T00:00:00Z'
    }
  ]
};

export const mockHealthProfile = {
  id: 1,
  userId: 1,
  familyId: 1,
  fullName: 'Nguyễn Văn Mock',
  dateOfBirth: '1990-01-01',
  gender: 'MALE',
  relationship: 'SELF',
  bloodType: 'O+',
  allergies: 'Không có',
  chronicDiseases: 'Không có',
  notes: '',
  avatarUrl: '',
  isChild: false,
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z'
};

export const mockAppointments = [
  {
    id: 1,
    healthProfileId: 1,
    doctorName: 'BS. Trần Mock',
    hospitalName: 'Bệnh viện Mock',
    address: '123 Đường Mock',
    appointmentDate: new Date(Date.now() + 86400000).toISOString(), // Tomorrow
    status: 'SCHEDULED',
    notes: 'Khám định kỳ',
    resultNotes: '',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z'
  }
];

export const mockMedications = [
  {
    id: 1,
    healthProfileId: 1,
    medicineName: 'Paracetamol',
    dosage: '500mg',
    frequency: 'DAILY',
    timesPerDay: 2,
    timeSlots: ['08:00', '20:00'],
    startDate: '2026-05-01',
    endDate: '2026-05-15',
    status: 'ACTIVE',
    notes: 'Uống sau ăn'
  }
];

export const mockMedicationLogs = [
  {
    id: 1,
    medicationId: 1,
    medicineName: 'Paracetamol',
    dosage: '500mg',
    scheduledTime: new Date(new Date().setHours(8, 0, 0, 0)).toISOString(),
    status: 'TAKEN',
    takenTime: new Date(new Date().setHours(8, 15, 0, 0)).toISOString(),
    notes: ''
  },
  {
    id: 2,
    medicationId: 1,
    medicineName: 'Paracetamol',
    dosage: '500mg',
    scheduledTime: new Date(new Date().setHours(20, 0, 0, 0)).toISOString(),
    status: 'PENDING',
    takenTime: null,
    notes: ''
  }
];

export const mockCabinet = {
  id: 1,
  familyId: 1,
  name: 'Tủ thuốc gia đình',
  medicines: [
    {
      id: 1,
      medicineName: 'Aspirin',
      quantity: 20,
      unit: 'Viên',
      expiryDate: '2027-01-01T00:00:00Z',
      status: 'AVAILABLE'
    }
  ]
};
