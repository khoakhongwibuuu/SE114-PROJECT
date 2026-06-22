import { apiGet, apiPost } from './client';

// ─── Types ────────────────────────────────────────────────────────────────────

export interface ChatUser {
  _id: number;
  name: string;
  avatar: string | null;
}

export interface ChatMessage {
  _id: number;
  text: string;
  createdAt: string; // ISO 8601
  user: ChatUser;
}

export interface ChatHistoryPage {
  content: ChatMessage[];
  totalPages: number;
  number: number;     // current page (0-indexed)
  last: boolean;
}

// ─── API Functions ────────────────────────────────────────────────────────────

/**
 * Lấy lịch sử chat của gia đình (có phân trang).
 * @param page 0-indexed page number
 */
export async function getChatHistory(
  familyId: number,
  page = 0,
  size = 20,
): Promise<ChatHistoryPage> {
  return apiGet<ChatHistoryPage>(
    `/families/${familyId}/messages?page=${page}&size=${size}&sort=createdAt,desc`,
  );
}
