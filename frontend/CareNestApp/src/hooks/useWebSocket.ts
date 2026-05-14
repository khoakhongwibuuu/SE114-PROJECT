import { useCallback, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage as StompFrame } from '@stomp/stompjs';
import type { IMessage as GiftedMessage } from 'react-native-gifted-chat';
import { getStoredSession } from '../api/storage';

// WebSocket URL: thay http:// → ws://, giữ nguyên path
// React Native dùng native WebSocket, KHÔNG qua SockJS
const WS_URL = 'ws://10.0.2.2:8080/api/v1/ws';

interface UseWebSocketOptions {
  familyId: number;
  onMessageReceived: (msg: GiftedMessage) => void;
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

      // ĐÂY LÀ ĐIỂM THEN CHỐT: JWT vào STOMP CONNECT header
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },

      reconnectDelay: 5000,       // Tự kết nối lại sau 5 giây nếu đứt
      heartbeatIncoming: 10000,   // Server ping mỗi 10s
      heartbeatOutgoing: 10000,   // Client ping mỗi 10s

      onConnect: () => {
        console.log('[WS] ✅ Kết nối thành công');

        // Subscribe vào kênh gia đình ngay sau khi connect
        client.subscribe(`/topic/family/${familyId}`, (frame: StompFrame) => {
          try {
            const raw = JSON.parse(frame.body);

            // Map sang IMessage của react-native-gifted-chat
            const msg: GiftedMessage = {
              _id: raw._id,
              text: raw.text,
              createdAt: new Date(raw.createdAt),
              user: {
                _id: raw.user._id,
                name: raw.user.name,
                avatar: raw.user.avatar ?? undefined,
              },
            };

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

    // QUAN TRỌNG: Cleanup — ngắt kết nối khi rời màn hình chat
    return () => {
      clientRef.current?.deactivate();
      clientRef.current = null;
    };
  }, [connect]);

  return { sendMessage, isConnected };
}
