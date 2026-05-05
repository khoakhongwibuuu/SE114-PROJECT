import { Platform } from 'react-native';

const API_HOST = Platform.select({
  android: 'http://10.0.2.2:8080',
  default: 'http://localhost:8080',
});

export const API_BASE_URL = `${API_HOST}/api/v1`;
