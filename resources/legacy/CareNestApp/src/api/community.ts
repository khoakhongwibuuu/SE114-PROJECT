import { apiDelete, apiGet, apiPost, invalidateApiGetCache, type PageResponse } from './client';

export interface Article {
  id: number;
  title: string;
  content: string;
  tags?: string | null;
  imageUrl?: string | null;
  authorId?: number | null;
  authorName?: string | null;
  authorAvatarUrl?: string | null;
  authorRole?: 'USER' | 'DOCTOR' | 'ADMIN' | string | null;
  authorSpecialty?: string | null;
  authorHospitalName?: string | null;
  authorPrivateGroupId?: number | null;
  authorSpecialtyGroupId?: number | null;
  createdAt?: string | null;
  likeCount?: number;
  commentCount?: number;
  likedByMe?: boolean;
}

export interface ArticleComment {
  id: number;
  articleId: number;
  authorId?: number | null;
  authorName?: string | null;
  content: string;
  createdAt?: string | null;
}

export interface ArticleLikeResult {
  articleId: number;
  likedByMe: boolean;
  likeCount: number;
}

export interface CommunityGroup {
  id: number;
  name: string;
  description?: string | null;
  category?: string | null;
  tags?: string | null;
  isPrivate?: boolean;
  leadDoctorId?: number | null;
  leadDoctorName?: string | null;
  memberCount?: number;
  joined?: boolean;
  latestMessage?: string | null;
  latestActivityAt?: string | null;
}

export interface CommunityGroupPreview extends CommunityGroup {
  memberCount: number;
  joined: boolean;
  myRole?: 'MEMBER' | 'HOST' | string | null;
  rules?: string | null;
}

export interface GroupPost {
  id: number;
  communityGroupId: number;
  communityGroupName?: string | null;
  authorId?: number | null;
  authorName?: string | null;
  authorRole?: 'USER' | 'DOCTOR' | 'ADMIN' | string | null;
  content: string;
  replyToPostId?: number | null;
  imageUrl?: string | null;
  createdAt?: string | null;
}

export interface CreateArticlePayload {
  title: string;
  content: string;
  tags?: string;
  imageUrl?: string;
}

export async function getArticles(): Promise<Article[]> {
  return apiGet<Article[]>('/articles');
}

export async function createArticle(payload: CreateArticlePayload): Promise<Article> {
  const article = await apiPost<Article, CreateArticlePayload>('/articles', payload);
  invalidateApiGetCache(['/articles']);
  return article;
}

export async function toggleArticleLike(articleId: number): Promise<ArticleLikeResult> {
  const result = await apiPost<ArticleLikeResult>(`/articles/${articleId}/like`);
  invalidateApiGetCache(['/articles']);
  return result;
}

export async function getArticleComments(articleId: number): Promise<ArticleComment[]> {
  return apiGet<ArticleComment[]>(`/articles/${articleId}/comments`);
}

export async function createArticleComment(articleId: number, content: string): Promise<ArticleComment> {
  const comment = await apiPost<ArticleComment, { content: string }>(`/articles/${articleId}/comments`, { content });
  invalidateApiGetCache(['/articles', `/articles/${articleId}/comments`]);
  return comment;
}

export async function getCommunityGroups(filters?: { search?: string; category?: string }): Promise<CommunityGroup[]> {
  return apiGet<CommunityGroup[]>('/communities', {
    search: filters?.search || undefined,
    category: filters?.category && filters.category !== 'Tất cả' ? filters.category : undefined,
  });
}

export async function getMyCommunityGroups(search?: string): Promise<CommunityGroup[]> {
  return apiGet<CommunityGroup[]>('/communities/my', { search: search || undefined });
}

export async function getDiscoverCommunityGroups(search?: string): Promise<CommunityGroup[]> {
  return apiGet<CommunityGroup[]>('/communities/discover', { search: search || undefined });
}

export async function getCommunityGroupPreview(groupId: number): Promise<CommunityGroupPreview> {
  return apiGet<CommunityGroupPreview>(`/communities/${groupId}/preview`);
}

export async function joinCommunityGroup(groupId: number): Promise<CommunityGroupPreview> {
  const preview = await apiPost<CommunityGroupPreview>(`/communities/${groupId}/join`);
  invalidateApiGetCache(['/communities', '/communities/my', '/communities/discover', `/communities/${groupId}/preview`]);
  return preview;
}

export async function leaveCommunityGroup(groupId: number): Promise<void> {
  await apiPost<void>(`/communities/${groupId}/leave`);
  invalidateApiGetCache(['/communities', '/communities/my', '/communities/discover', `/communities/${groupId}/preview`, `/communities/${groupId}/posts`]);
}

export async function getGroupPosts(
  groupId: number,
  page = 0,
  size = 30,
): Promise<PageResponse<GroupPost>> {
  return apiGet<PageResponse<GroupPost>>(`/communities/${groupId}/posts`, { page, size, sort: 'createdAt,desc' });
}

export async function createGroupPost(
  groupId: number,
  content: string,
  options?: { replyToPostId?: number; imageUrl?: string },
): Promise<GroupPost> {
  const post = await apiPost<GroupPost, { content: string; replyToPostId?: number; imageUrl?: string }>(
    `/communities/${groupId}/posts`,
    { content, replyToPostId: options?.replyToPostId, imageUrl: options?.imageUrl },
  );
  invalidateApiGetCache([`/communities/${groupId}/posts`]);
  return post;
}

export async function reportGroupPost(postId: number, reason: string): Promise<void> {
  await apiPost<void, { reason: string }>(`/posts/${postId}/report`, { reason });
}

export async function kickCommunityMember(groupId: number, targetUserId: number): Promise<void> {
  await apiDelete<void>(`/communities/${groupId}/members/${targetUserId}`);
  invalidateApiGetCache([`/communities/${groupId}/preview`, `/communities/${groupId}/posts`]);
}
