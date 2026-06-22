import React, { ReactNode } from 'react';
import { useAuth } from '../../context/AuthContext';

interface RoleGuardProps {
  allowedRoles: string[];
  children: ReactNode;
}

export default function RoleGuard({ allowedRoles, children }: RoleGuardProps) {
  const { user } = useAuth();

  if (!user || !user.role) {
    return null;
  }

  if (allowedRoles.includes(user.role)) {
    return <>{children}</>;
  }

  return null;
}
