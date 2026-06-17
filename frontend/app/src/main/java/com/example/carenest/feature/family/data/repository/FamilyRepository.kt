package com.example.carenest.feature.family.data.repository

import com.example.carenest.feature.family.domain.model.*
import com.example.carenest.feature.family.data.remote.FamilyApi
import com.example.carenest.core.data.network.errorMessage
import com.example.carenest.core.data.network.requireData
import com.example.carenest.core.data.network.requireList
import com.example.carenest.core.data.network.requireSuccess
import com.example.carenest.core.data.network.userMessage
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.model.HealthProfile
import com.example.carenest.model.MedicalCondition
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class FamilyRepository(
    private val familyApi: FamilyApi,
    private val dataStoreManager: SecureSessionManager
) {
    suspend fun getMyFamilyList(): Result<List<FamilySummary>> {
        return try {
            val response = familyApi.getMyFamilyList()
            Result.success(response.requireList("Không thể tải danh sách gia đình"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyById(familyId: Long): Result<FamilyDetailResponse> {
        return try {
            val response = familyApi.getFamilyById(familyId)
            Result.success(response.requireData("Không thể tải thông tin gia đình").normalized())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyProfile(profileId: Long): Result<HealthProfile> {
        return try {
            val response = familyApi.getFamilyProfile(profileId)
            val raw = response.requireData("Không thể tải hồ sơ sức khỏe")
            val bmiValue = if (raw.weight != null && raw.height != null && raw.height > 0) {
                val heightM = raw.height / 100f
                raw.weight / (heightM * heightM)
            } else null

            // Parse conditions
            val conditionsList = mutableListOf<MedicalCondition>()
            if (!raw.chronicDiseases.isNullOrEmpty()) {
                raw.chronicDiseases.split(";").forEach {
                    if (it.isNotBlank()) conditionsList.add(MedicalCondition(it.trim(), "Theo dõi định kỳ"))
                }
            }

            // Parse allergies
            val allergiesList = if (!raw.allergies.isNullOrEmpty()) {
                raw.allergies.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else emptyList()

            // Calculate Age
            var calcAge: Int? = null
            if (!raw.dateOfBirth.isNullOrEmpty()) {
                try {
                    val year = raw.dateOfBirth.substring(0, 4).toInt()
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    calcAge = currentYear - year
                } catch (e: Exception) {}
            }
            
            val profileId = raw.id
                ?: return Result.failure(Exception("Profile response is missing id"))

            val profile = HealthProfile(
                id = profileId,
                name = raw.fullName,
                role = raw.relationship.orEmpty(),
                age = calcAge,
                dateOfBirth = raw.dateOfBirth,
                gender = raw.gender,
                location = null,
                avatarUrl = raw.avatarUrl,
                isVerified = true,
                bloodType = raw.bloodType,
                allergies = allergiesList,
                height = raw.height,
                weight = raw.weight,
                bmi = bmiValue,
                medicalHistory = conditionsList,
                emergencyContact = null
            )
            Result.success(profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFamily(name: String): Result<FamilyResponse> {
        return try {
            val response = familyApi.createFamily(CreateFamilyRequest(name))
            Result.success(response.requireData("Không thể tạo gia đình"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamilyByCode(code: String, role: String? = null): Result<FamilyDetailResponse> {
        return try {
            val response = familyApi.joinFamilyByCode(JoinFamilyByCodeRequest(code, role))
            Result.success(response.requireData("Không thể tham gia gia đình"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamilyByQr(file: File, role: String): Result<FamilyDetailResponse> {
        return try {
            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)
            val response = familyApi.joinFamilyByQr(imagePart, role)
            Result.success(response.requireData("Không thể tham gia gia đình bằng QR"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inviteMember(familyId: Long, email: String, role: String): Result<Unit> {
        return try {
            val response = familyApi.inviteMember(familyId, InviteMemberRequest(email, role))
            response.requireSuccess("Không thể gửi lời mời")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedInvitations(): Result<List<FamilyInvitationItem>> {
        return try {
            val response = familyApi.getReceivedInvitations()
            Result.success(response.requireList("Không thể tải lời mời đã nhận"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentInvitations(): Result<List<FamilyInvitationItem>> {
        return try {
            val response = familyApi.getSentInvitations()
            Result.success(response.requireList("Không thể tải lời mời đã gửi"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptInvitation(inviteId: Long): Result<Unit> {
        return try {
            val response = familyApi.updateInvitationStatus(inviteId, UpdateInvitationRequest("ACCEPTED"))
            response.requireSuccess("Không thể chấp nhận lời mời")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectInvitation(inviteId: Long): Result<Unit> {
        return try {
            val response = familyApi.updateInvitationStatus(inviteId, UpdateInvitationRequest("REJECTED"))
            response.requireSuccess("Không thể từ chối lời mời")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyJoinCode(): Result<FamilyJoinCodeResponse> {
        return try {
            val response = familyApi.getFamilyJoinCode()
            Result.success(response.requireData("Không thể tải mã gia đình"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rotateFamilyJoinCode(): Result<FamilyJoinCodeResponse> {
        return try {
            val response = familyApi.rotateFamilyJoinCode()
            Result.success(response.requireData("Không thể tạo lại mã gia đình"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveActiveFamilyId(id: String) {
        dataStoreManager.saveFamilyId(id)
    }

    suspend fun getActiveFamilyId(): String? {
        return dataStoreManager.familyIdFlow.first()
    }

    suspend fun updateProfile(
        profileId: Long,
        fullName: String,
        birthday: String?,
        gender: String?,
        relationship: String,
        height: Double?,
        weight: Double?,
        bloodType: String?,
        allergy: String?,
        medicalHistory: String?
    ): Result<Unit> {
        return try {
            val detailsResponse = familyApi.updateProfileDetails(
                profileId,
                UpdateProfileDetailsRequest(
                    fullName = fullName,
                    dateOfBirth = birthday,
                    gender = gender,
                    relationship = relationship,
                    isChild = false,
                    height = height,
                    weight = weight
                )
            )
            if (!detailsResponse.isSuccessful) {
                return Result.failure(Exception(detailsResponse.errorMessage("Không thể cập nhật thông tin hồ sơ")))
            }
            runCatching {
                detailsResponse.body().requireSuccess("Không thể cập nhật thông tin hồ sơ")
            }.onFailure { return Result.failure(Exception(it.userMessage("Không thể cập nhật thông tin hồ sơ"), it)) }
            val medResponse = familyApi.updateProfileMedicalInfo(
                profileId,
                UpdateMedicalInfoRequest(
                    bloodType = bloodType,
                    allergies = allergy,
                    chronicDiseases = medicalHistory
                )
            )
            if (!medResponse.isSuccessful) {
                return Result.failure(Exception(medResponse.errorMessage("Không thể cập nhật thông tin y tế")))
            }
            runCatching {
                medResponse.body().requireSuccess("Không thể cập nhật thông tin y tế")
            }.onFailure { return Result.failure(Exception(it.userMessage("Không thể cập nhật thông tin y tế"), it)) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun FamilyDetailResponse.normalized(): FamilyDetailResponse {
        val normalizedMembers = members.map { member ->
            member.copy(
                familyMemberId = member.familyMemberId ?: member.id,
                userId = member.userId ?: member.user?.id
            )
        }
        return copy(
            memberCount = if (memberCount > 0) memberCount else normalizedMembers.size,
            members = normalizedMembers
        )
    }
}
