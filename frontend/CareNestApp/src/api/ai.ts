import { apiClient, apiGet, apiPost } from './client';
import { createCabinetMedicine, createMedicineSchedule } from './medicine';

export interface ChatReply {
  reply: string;
  id: number;
  messageId?: number;
  message_id?: number;
  conversation_id?: number;
  sqlGenerated?: string | null;
  data?: unknown;
}

export interface OcrReply {
  rawText: string;
  structuredData: {
    medicines: Array<{
      name: string;
      dosage?: string | null;
      frequency?: number | null;
      duration?: string | null;
      note?: string | null;
    }>;
    doctorName?: string | null;
    clinicName?: string | null;
    date?: string | null;
  };
  id?: number | null;
}

export interface VoiceReply {
  transcribedText: string;
  replyText: string;
  audioBase64: string;
  id: number;
}


export async function chatAi(payload: { message: string; conversationId?: number | null; profileId?: number | null }): Promise<ChatReply> {
  return apiPost<ChatReply, { message: string; conversationId?: number | null; profileId?: number | null }>('/ai/chat', payload);
}

export async function listConversations(): Promise<{ conversations: Array<Record<string, unknown>>; total: number }> {
  return apiGet('/ai/conversations');
}

export async function getConversationMessages(conversationId: number): Promise<{ conversation_id: number; messages: Array<Record<string, unknown>> }> {
  return apiGet(`/ai/conversations/${conversationId}/messages`);
}

export async function submitOcr(payload: { profileId: number; imageBase64: string }): Promise<OcrReply> {
  console.log('[MOCK OCR] Nhận diện đơn thuốc...');
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log('[MOCK OCR] Trả về dữ liệu đơn thuốc giả lập...');
      resolve({
        id: 9999,
        rawText: 'DON THUOC GIA LAP\nPhòng khám CareNest\nBác sĩ: Nguyễn Văn A\n\n1. Panadol Extra 500mg\nSố lượng: 20 viên\nSáng 1 viên, Tối 1 viên, Sau ăn\n\n2. Amoxicillin 250mg\nSố lượng: 15 viên\nSáng 1 viên, Trưa 1 viên, Chiều 1 viên, Sau ăn',
        structuredData: {
          clinicName: 'Phòng khám Đa khoa Quốc tế CareNest',
          doctorName: 'Nguyễn Văn A',
          date: new Date().toLocaleDateString('vi-VN'),
          medicines: [
            {
              name: 'Panadol Extra 500mg',
              dosage: '1 viên',
              frequency: 2,
              duration: '10 ngày',
              note: 'Uống sáng, tối sau khi ăn',
            },
            {
              name: 'Amoxicillin 250mg',
              dosage: '1 viên',
              frequency: 3,
              duration: '5 ngày',
              note: 'Uống sáng, trưa, tối sau khi ăn',
            },
          ],
        },
      });
    }, 2000);
  });
}

export async function confirmOcr(ocrId: number, payload: { profileId: number; structuredData: Record<string, unknown> }): Promise<{
  ocrId?: number;
  medicineIds: number[];
  scheduleIds: number[];
}> {
  console.log('[MOCK OCR] Bắt đầu đồng bộ dữ liệu Mock OCR E2E vào cơ sở dữ liệu thật...');
  
  const medicines = (payload.structuredData.medicines as any[]) || [];
  
  for (const med of medicines) {
    try {
      // 1. Đồng bộ vào Tủ thuốc gia đình thật
      let quantity = 20;
      let unit = 'Viên';
      
      const lowerName = (med.name || '').toLowerCase();
      if (lowerName.includes('panadol')) {
        quantity = 20;
      } else if (lowerName.includes('amoxicillin')) {
        quantity = 15;
      }
      
      await createCabinetMedicine({
        name: med.name,
        quantity,
        unit,
      });
      console.log(`[MOCK OCR] Đã thêm ${med.name} vào Tủ thuốc gia đình.`);
      
      // 2. Đồng bộ vào Kế hoạch nhắc nhở uống thuốc thật
      const startDate = new Date().toISOString().split('T')[0];
      let days = 7;
      if (med.duration) {
        const match = med.duration.match(/(\d+)/);
        if (match) {
          days = parseInt(match[1], 10);
        }
      }
      const endDateObj = new Date();
      endDateObj.setDate(endDateObj.getDate() + days);
      const endDate = endDateObj.toISOString().split('T')[0];
      
      await createMedicineSchedule({
        profile: payload.profileId,
        medicineId: 0,
        medicineName: med.name,
        dosage: med.dosage || '1 viên',
        frequency: Number(med.frequency) || 1,
        note: med.note || 'Sau ăn',
        startDate,
        endDate,
      });
      console.log(`[MOCK OCR] Đã thêm lịch nhắc nhở cho ${med.name}.`);
    } catch (e) {
      console.error('[MOCK OCR] Lỗi khi đồng bộ thuốc:', med.name, e);
    }
  }
  
  return {
    ocrId,
    medicineIds: [1, 2],
    scheduleIds: [1, 2],
  };
}

export async function voiceChat(payload: FormData): Promise<VoiceReply> {
  const response = await apiClient.post('/ai/voice/chat', payload, {
    headers: { Accept: 'application/json' },
    timeout: 300000,
  });
  return response.data.data as VoiceReply;
}

export async function speakText(text: string): Promise<string> {
  const response = await apiClient.post(
    '/ai/voice/tts',
    { text, lang: 'vi' },
    { timeout: 60000 },
  );
  return (response.data.data as { audio_base64: string }).audio_base64;
}
