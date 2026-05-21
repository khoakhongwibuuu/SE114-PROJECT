import { Platform } from 'react-native';
import Config from 'react-native-config';

const localHost = 'localhost';

const defaultApiUrl = `http://${localHost}:8080/api/v1`;
const defaultWsUrl = `ws://${localHost}:8080/api/v1/ws`;

export const API_BASE_URL = Config.API_URL || Config.API_BASE_URL || defaultApiUrl;
export const WS_BASE_URL = Config.WS_URL || Config.WS_BASE_URL || defaultWsUrl;
