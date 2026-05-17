import Config from 'react-native-config';

// Sử dụng biến môi trường từ file .env (ưu tiên) hoặc fallback về emulator local
export const API_BASE_URL = Config.API_URL || 'http://10.0.2.2:8080/api/v1';
