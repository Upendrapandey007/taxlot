import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

// Default to localhost for Android emulator / iOS simulator, or can be set dynamically
let customApiUrl: string | null = null;

export function setCustomApiUrl(url: string) {
  customApiUrl = url;
}

export function getBaseApiUrl(): string {
  if (customApiUrl) return customApiUrl;
  // Android emulator uses 10.0.2.2 to reach host machine
  if (Platform.OS === 'android') {
    return 'http://10.0.2.2:8080';
  }
  return 'http://localhost:8080';
}

export async function getAccessToken(): Promise<string | null> {
  try {
    return await SecureStore.getItemAsync('taxlot_access_token');
  } catch {
    return null;
  }
}

export async function setTokens(accessToken: string, refreshToken: string) {
  try {
    await SecureStore.setItemAsync('taxlot_access_token', accessToken);
    await SecureStore.setItemAsync('taxlot_refresh_token', refreshToken);
  } catch (e) {
    console.error('Failed to store tokens', e);
  }
}

export async function clearTokens() {
  try {
    await SecureStore.deleteItemAsync('taxlot_access_token');
    await SecureStore.deleteItemAsync('taxlot_refresh_token');
  } catch (e) {
    console.error('Failed to clear tokens', e);
  }
}

export async function apiRequest<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = await getAccessToken();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const url = `${getBaseApiUrl()}${endpoint}`;
  const response = await fetch(url, { ...options, headers });

  if (!response.ok) {
    let errorData = { message: 'Network request failed', code: 'ERROR' };
    try {
      errorData = await response.json();
    } catch {}
    throw new Error(errorData.message || `Request failed with status ${response.status}`);
  }

  if (response.status === 204) {
    return {} as T;
  }

  return response.json();
}
