import { useCallback, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage as StompFrame } from '@stomp/stompjs';
import { getStoredSession } from '../api/storage';
import { WS_BASE_URL } from '../api/config';

// WebSocket URL: Lấy từ .env hoặc fallback thông minh theo hệ điều hành
const WS_URL = WS_BASE_URL;

// Self-contained payload type — không phụ thuộc thư viện UI nào
export interface ChatMessagePayload {
  _id: string | number;
  text: string;
  createdAt: Date;
  user: {
    _id: number;
    name: string;
    avatar?: string;
  };
}

interface UseWebSocketOptions {
  familyId: number;
  onMessageReceived: (msg: ChatMessagePayload) => void;
}

interface UseWebSocketReturn {
  sendMessage: (content: string) => void;
  isConnected: () => boolean;
}

export function useWebSocket({
  familyId,
  onMessageReceived,
}: UseWebSocketOptions): UseWebSocketReturn {
  const clientRef = useRef<Client | null>(null);

  const connect = useCallback(async () => {
    const session = await getStoredSession();
    const token = session?.token;
    if (!token) {
      console.warn('[WS] Không có token, bỏ qua kết nối.');
      return;
    }

    const client = new Client({
      brokerURL: WS_URL,

      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      // ĐÂY LÀ ĐIỂM SỐNG CÒN CHO REACT NATIVE ANDROID
      // Tránh lỗi gửi ký tự NULL (\0) qua TEXT frame làm crash server
      forceBinaryWSFrames: true,
      appendMissingNULLonIncoming: true,

      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        console.log('[WS] ✅ Kết nối thành công');

        client.subscribe(`/topic/family/${familyId}`, (frame: StompFrame) => {
          try {
            const raw = JSON.parse(frame.body);
            console.log('[WS] 📨 Raw message received:', JSON.stringify(raw));

            // Backend dùng Java Instant -> có thể là ISO string hoặc epoch ms
            const createdAt = raw.createdAt
              ? new Date(raw.createdAt)
              : new Date();

            const msg: ChatMessagePayload = {
              _id: raw._id ?? raw.id ?? Date.now(),
              text: raw.text ?? raw.content ?? '',
              createdAt,
              user: {
                _id: raw.user?._id ?? raw.user?.id ?? 0,
                name: raw.user?.name ?? 'Unknown',
                avatar: raw.user?.avatar ?? undefined,
              },
            };

            console.log('[WS] ✅ Parsed message:', JSON.stringify(msg));
            onMessageReceived(msg);
          } catch (e) {
            console.error('[WS] Lỗi parse tin nhắn:', e);
          }
        });
      },

      onDisconnect: () => console.log('[WS] 🔌 Ngắt kết nối'),
      onStompError: (frame) =>
        console.error('[WS] ❌ STOMP Error:', frame.headers['message']),
    });

    client.activate();
    clientRef.current = client;
  }, [familyId, onMessageReceived]);

  const sendMessage = useCallback(
    (content: string) => {
      if (clientRef.current?.connected) {
        clientRef.current.publish({
          destination: '/app/chat.sendMessage',
          headers: { 'content-type': 'application/json' },
          body: JSON.stringify({ familyId, content }),
        });
      } else {
        console.warn('[WS] Chưa kết nối, không thể gửi tin nhắn.');
      }
    },
    [familyId],
  );

  const isConnected = useCallback(
    () => clientRef.current?.connected ?? false,
    [],
  );

  useEffect(() => {
    void connect();

    return () => {
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [connect]);

  return { sendMessage, isConnected };
}
