package com.example.carenest.feature.family.domain.port

import com.example.carenest.feature.family.domain.model.FamilyDetailResponse
import com.example.carenest.feature.family.domain.model.FamilyInvitationItem
import com.example.carenest.feature.family.domain.model.FamilyJoinCodeResponse
import com.example.carenest.feature.family.domain.model.FamilyResponse
import com.example.carenest.feature.family.domain.model.FamilySummary
import java.io.File

interface FamilyDataSource {
    suspend fun getMyFamilyList(): Result<List<FamilySummary>>
    suspend fun getFamilyById(familyId: Long): Result<FamilyDetailResponse>
    suspend fun createFamily(name: String): Result<FamilyResponse>
    suspend fun joinFamilyByCode(code: String, role: String? = null): Result<FamilyDetailResponse>
    suspend fun joinFamilyByQr(file: File, role: String): Result<FamilyDetailResponse>
    suspend fun inviteMember(familyId: Long, email: String, role: String): Result<Unit>
    suspend fun getReceivedInvitations(): Result<List<FamilyInvitationItem>>
    suspend fun getSentInvitations(): Result<List<FamilyInvitationItem>>
    suspend fun acceptInvitation(inviteId: Long): Result<Unit>
    suspend fun rejectInvitation(inviteId: Long): Result<Unit>
    suspend fun getFamilyJoinCode(): Result<FamilyJoinCodeResponse>
    suspend fun rotateFamilyJoinCode(): Result<FamilyJoinCodeResponse>
    suspend fun saveActiveFamilyId(id: String)
    suspend fun getActiveFamilyId(): String?
}
