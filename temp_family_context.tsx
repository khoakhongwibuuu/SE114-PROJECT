import React, {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { DeviceEventEmitter } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {
  createFamily as createFamilyRequest,
  getMyFamily,
  getMyFamilyList,
  getFamilyById,
  type FamilyMemberSummary,
  type FamilyDetailResponse,
  type FamilySummary,
} from '../api/family';
import { ACTIVE_FAMILY_STORAGE_KEY, setActiveFamilyHeaderId } from '../api/activeFamily';
import { useAuth } from './AuthContext';

interface FamilyContextType {
  // ΓöÇΓöÇ Legacy / Active-family state ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  hasFamily: boolean;
  family: FamilyDetailResponse | null;
  familyName: string;
  familyImage: string | null;
  members: FamilyMemberSummary[];
  selectedProfileId: number | null;
  setSelectedProfileId: (profileId: number | null) => void;
  ownProfileId: number | null;

  // ΓöÇΓöÇ Multi-family ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  /** Summary list of every family the current user belongs to */
  allFamilies: FamilySummary[];
  /** The family currently being "viewed" across the whole app */
  activeFamilyId: number | null;
  /** Switch the active family ΓÇö persists to AsyncStorage automatically */
  setActiveFamilyId: (id: number) => Promise<void>;

  // ΓöÇΓöÇ Actions ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  createFamily: (name: string, image: string | null) => Promise<void>;
  resetFamily: () => void;
  refreshFamily: () => Promise<void>;
}

const FamilyContext = createContext<FamilyContextType | undefined>(undefined);

export function FamilyProvider({ children }: { children: ReactNode }) {
  const { isLoggedIn, user } = useAuth();

  // ΓöÇΓöÇ Core family state ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const [family, setFamily] = useState<FamilyDetailResponse | null>(null);
  const [familyImage, setFamilyImage] = useState<string | null>(null);
  const [selectedProfileId, setSelectedProfileId] = useState<number | null>(null);
  const [hasInitializedSelection, setHasInitializedSelection] = useState(false);

  const ownProfileId = useMemo(() => {
    if (!family?.members?.length) return null;
    const currentUserMember = family.members.find(m => m.userId === user?.userId);
    return currentUserMember?.profileId || (user?.profileId ? Number(user.profileId) : null);
  }, [family?.members, user?.userId, user?.profileId]);

  // ΓöÇΓöÇ Multi-family state ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const [allFamilies, setAllFamilies] = useState<FamilySummary[]>([]);
  const [activeFamilyId, setActiveFamilyIdState] = useState<number | null>(null);
  const isBootstrapping = useRef(false);

  // ΓöÇΓöÇ Auto-select member profile when family loads ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  useEffect(() => {
    if (!hasInitializedSelection && family?.members?.length) {
      const currentUserMember = family.members.find(m => m.userId === user?.userId);
      const preferredProfileId = currentUserMember?.profileId 
        || (user?.profileId ? Number(user.profileId) : family.members[0].id);
      setSelectedProfileId(preferredProfileId);
      setHasInitializedSelection(true);
    }
  }, [family, hasInitializedSelection, user?.profileId, user?.userId]);

  // ΓöÇΓöÇ Bootstrap: load family list + resolve active family ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const bootstrapFamilies = useCallback(async () => {
    if (isBootstrapping.current) return;
    isBootstrapping.current = true;

    try {
      const [list] = await Promise.all([getMyFamilyList()]);
      setAllFamilies(list);

      if (list.length === 0) {
        setActiveFamilyIdState(null);
        setActiveFamilyHeaderId(null);
        await AsyncStorage.removeItem(ACTIVE_FAMILY_STORAGE_KEY);
        return;
      }

      // Read persisted selection
      const saved = await AsyncStorage.getItem(ACTIVE_FAMILY_STORAGE_KEY);
      const savedId = saved ? Number(saved) : null;
      const stillValid = savedId !== null && list.some(f => f.id === savedId);

      // Edge Case: user was kicked ΓåÆ auto-recover to first available family
      // Prefer the family where they are OWNER, then fall back to first
      const preferred =
        list.find(f => f.myRole === 'OWNER')?.id ?? list[0].id;
      const resolvedId = stillValid ? savedId : preferred;

      setActiveFamilyIdState(resolvedId);
      setActiveFamilyHeaderId(resolvedId);
      await AsyncStorage.setItem(ACTIVE_FAMILY_STORAGE_KEY, String(resolvedId));

      // Load full detail for the active family
      const detail = await getFamilyById(resolvedId);
      setFamily(detail);
    } catch {
      // Network error ΓÇö keep stale state, don't crash the app
    } finally {
      isBootstrapping.current = false;
    }
  }, []);

  const refreshFamily = useCallback(async () => {
    if (!isLoggedIn) return;
    try {
      // Refresh the list
      const list = await getMyFamilyList();
      setAllFamilies(list);
      if (list.length === 0) {
        setFamily(null);
        setActiveFamilyIdState(null);
        setActiveFamilyHeaderId(null);
        setSelectedProfileId(user?.profileId ? Number(user.profileId) : null);
        setHasInitializedSelection(false);
        await AsyncStorage.removeItem(ACTIVE_FAMILY_STORAGE_KEY);
        return;
      }

      // Refresh the active family detail using legacy API as fallback
      const nextFamily = activeFamilyId
        ? await getFamilyById(activeFamilyId)
        : await getMyFamily({ forceRefresh: true });
      setFamily(nextFamily);
    } catch {
      setFamily(null);
      setSelectedProfileId(user?.profileId ? Number(user.profileId) : null);
      setHasInitializedSelection(false);
    }
  }, [isLoggedIn, user?.profileId, activeFamilyId]);

  useEffect(() => {
    if (isLoggedIn && user?.role !== 'ADMIN') {
      void bootstrapFamilies();
    } else if (!isLoggedIn) {
      resetFamily();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isLoggedIn, user?.role]);

  // ΓöÇΓöÇ Listen for BOLA/IDOR kicks (403 Forbidden) to recover gracefully ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  // Skip recovery for ADMIN ΓÇö they have no family, so 403s are expected
  useEffect(() => {
    if (user?.role === 'ADMIN') return;
    const handler = () => {
      console.log('[FamilyContext] Recovering from 403 access denied...');
      void bootstrapFamilies();
    };
    const subscription = DeviceEventEmitter.addListener('FAMILY_ACCESS_DENIED', handler);
    return () => subscription.remove();
  }, [bootstrapFamilies, user?.role]);

  // ΓöÇΓöÇ Public: switch active family ΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇΓöÇ
  const setActiveFamilyId = useCallback(async (id: number) => {
    setActiveFamilyIdState(id);
    setActiveFamilyHeaderId(id);
    await AsyncStorage.setItem(ACTIVE_FAMILY_STORAGE_KEY, String(id));
    try {
      const detail = await getFamilyById(id);
      setFamily(detail);
      // Reset member selection to the logged-in user's profile within new family
      setHasInitializedSelection(false);
      setSelectedProfileId(null);
    } catch {
      // keep stale
    }
  }, []);

  async function createFamily(name: string, image: string | null) {
    await createFamilyRequest(name);
    setFamilyImage(image);
    await bootstrapFamilies();
  }

  function resetFamily() {
    setFamily(null);
    setFamilyImage(null);
    setAllFamilies([]);
    setActiveFamilyIdState(null);
    setActiveFamilyHeaderId(null);
    setSelectedProfileId(null);
    setHasInitializedSelection(false);
    void AsyncStorage.removeItem(ACTIVE_FAMILY_STORAGE_KEY);
  }

  return (
    <FamilyContext.Provider
      value={{
        hasFamily: Boolean(family),
        family,
        familyName: family?.name || 'Tß╗ò ß║Ñm th├ón th╞░╞íng',
        familyImage,
        members: family?.members || [],
        selectedProfileId,
        setSelectedProfileId,
        ownProfileId,
        allFamilies,
        activeFamilyId,
        setActiveFamilyId,
        createFamily,
        resetFamily,
        refreshFamily,
      }}
    >
      {children}
    </FamilyContext.Provider>
  );
}

export function useFamily() {
  const context = useContext(FamilyContext);
  if (!context) {
    throw new Error('useFamily must be used within a FamilyProvider');
  }
  return context;
}
