package com.example.carenest.feature.family.data.repository

import com.example.carenest.feature.family.domain.model.*
import com.example.carenest.feature.family.data.remote.FamilyApi
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.model.HealthProfile
import com.example.carenest.model.MedicalCondition
import com.example.carenest.model.EmergencyContact
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FamilyRepository(
    private val familyApi: FamilyApi,
    private val dataStoreManager: SecureSessionManager
) {
    suspend fun getMyFamilyList(): Result<List<FamilySummary>> {
        return try {
            val response = familyApi.getMyFamilyList()
            if (response.isSuccessful) {
                Result.success(response.body()?.data.orEmpty())
            } else {
                Result.failure(Exception("Failed to fetch family list: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyById(familyId: Long): Result<FamilyDetailResponse> {
        return try {
            val response = familyApi.getFamilyById(familyId)
            val family = response.body()?.data
            if (response.isSuccessful && family != null) {
                Result.success(family)
            } else {
                Result.failure(Exception("Failed to fetch family: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyProfile(profileId: Long): Result<HealthProfile> {
        return try {
            val response = familyApi.getFamilyProfile(profileId)
            val raw = response.body()?.data
            if (response.isSuccessful && raw != null) {
                val bmiValue = if (raw.weight != null && raw.height != null && raw.height > 0) {
                    val heightM = raw.height / 100f
                    raw.weight / (heightM * heightM)
                } else null

                // Parse conditions
                val conditionsList = mutableListOf<MedicalCondition>()
                if (!raw.chronicDiseases.isNullOrEmpty()) {
                    raw.chronicDiseases.split(";").forEach {
                        if (it.isNotBlank()) conditionsList.add(MedicalCondition(it.trim(), "Theo doi dinh ky"))
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
                
                // Emergency Contact Mock based on phone
                val emContact = if (!raw.emergencyContactPhone.isNullOrEmpty()) {
                    EmergencyContact("Nguoi than", "Nguoi nha", raw.emergencyContactPhone)
                } else null

                val profile = HealthProfile(
                    id = raw.id,
                    name = raw.fullName,
                    role = raw.relationship,
                    age = calcAge,
                    location = "Ho Chi Minh", // Mocked as requested
                    avatarUrl = raw.avatarUrl,
                    isVerified = true,
                    bloodType = raw.bloodType,
                    allergies = allergiesList,
                    height = raw.height,
                    weight = raw.weight,
                    bmi = bmiValue,
                    medicalHistory = conditionsList,
                    emergencyContact = emContact
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to fetch profile: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createFamily(name: String): Result<Unit> {
        return try {
            val response = familyApi.createFamily(CreateFamilyRequest(name))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to create family"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamilyByCode(code: String, role: String? = null): Result<FamilyDetailResponse> {
        return try {
            val response = familyApi.joinFamilyByCode(JoinFamilyByCodeRequest(code, role))
            val family = response.body()?.data
            if (response.isSuccessful && family != null) {
                Result.success(family)
            } else {
                Result.failure(Exception("Failed to join family"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinFamilyByQr(file: File, role: String): Result<FamilyDetailResponse> {
        return try {
            val reqFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, reqFile)
            val response = familyApi.joinFamilyByQr(imagePart, role)
            val family = response.body()?.data
            if (response.isSuccessful && family != null) {
                Result.success(family)
            } else {
                Result.failure(Exception("Failed to join family via QR"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun inviteMember(familyId: Long, email: String, role: String): Result<Unit> {
        return try {
            val response = familyApi.inviteMember(familyId, InviteMemberRequest(email, role))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to invite member"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReceivedInvitations(): Result<List<FamilyInvitationItem>> {
        return try {
            val response = familyApi.getReceivedInvitations()
            if (response.isSuccessful) {
                Result.success(response.body()?.data.orEmpty())
            } else {
                Result.failure(Exception("Failed to fetch received invitations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSentInvitations(): Result<List<FamilyInvitationItem>> {
        return try {
            val response = familyApi.getSentInvitations()
            if (response.isSuccessful) {
                Result.success(response.body()?.data.orEmpty())
            } else {
                Result.failure(Exception("Failed to fetch sent invitations"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptInvitation(inviteId: Int): Result<Unit> {
        return try {
            val response = familyApi.updateInvitationStatus(inviteId, UpdateInvitationRequest("ACCEPTED"))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to accept invitation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectInvitation(inviteId: Int): Result<Unit> {
        return try {
            val response = familyApi.updateInvitationStatus(inviteId, UpdateInvitationRequest("REJECTED"))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to reject invitation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFamilyJoinCode(): Result<FamilyJoinCodeResponse> {
        return try {
            val response = familyApi.getFamilyJoinCode()
            val joinCode = response.body()?.data
            if (response.isSuccessful && joinCode != null) {
                Result.success(joinCode)
            } else {
                Result.failure(Exception("Failed to get join code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rotateFamilyJoinCode(): Result<FamilyJoinCodeResponse> {
        return try {
            val response = familyApi.rotateFamilyJoinCode()
            val joinCode = response.body()?.data
            if (response.isSuccessful && joinCode != null) {
                Result.success(joinCode)
            } else {
                Result.failure(Exception("Failed to rotate join code"))
            }
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
                return Result.failure(Exception("Failed to update profile details"))
            }
            val medResponse = familyApi.updateProfileMedicalInfo(
                profileId,
                UpdateMedicalInfoRequest(
                    bloodType = bloodType,
                    allergies = allergy,
                    chronicDiseases = medicalHistory
                )
            )
            if (!medResponse.isSuccessful) {
                return Result.failure(Exception("Failed to update profile medical info"))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
