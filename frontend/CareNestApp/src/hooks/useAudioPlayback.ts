import { useCallback, useMemo, useState } from 'react';

export function useAudioPlayback() {
  const [isPlaying] = useState(false);
  const [lastAudioPath] = useState<string | null>(null);

  const stopPlayback = useCallback(async () => {}, []);

  const playBase64 = useCallback(async (_audioBase64: string): Promise<string | null> => {
    console.warn('useAudioPlayback: Audio playback is not supported in this build.');
    return null;
  }, []);

  return useMemo(
    () => ({
      isPlaying,
      lastAudioPath,
      playBase64,
      stopPlayback,
    }),
    [isPlaying, lastAudioPath, playBase64, stopPlayback],
  );
}
