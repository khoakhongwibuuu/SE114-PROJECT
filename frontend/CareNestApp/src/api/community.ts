import { apiGet, apiPost, invalidateApiGetCache, type PageResponse } from './client';

export interface Article {
  id: number;
  title: string;
  content: string;
  tags?: string | null;
  imageUrl?: string | null;
  authorId?: number | null;
  authorName?: string | null;
  createdAt?: string | null;
}

export interface CommunityGroup {
  id: number;
  name: string;
  description?: string | null;
}

export interface GroupPost {
  id: number;
  communityGroupId: number;
  communityGroupName?: string | null;
  authorId?: number | null;
  authorName?: string | null;
  content: string;
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

export async function getCommunityGroups(): Promise<CommunityGroup[]> {
  return apiGet<CommunityGroup[]>('/communities');
}

export async function getGroupPosts(
  groupId: number,
  page = 0,
  size = 30,
): Promise<PageResponse<GroupPost>> {
  return apiGet<PageResponse<GroupPost>>(`/communities/${groupId}/posts`, { page, size, sort: 'createdAt,desc' });
}

export async function createGroupPost(groupId: number, content: string): Promise<GroupPost> {
  const post = await apiPost<GroupPost, { content: string }>(`/communities/${groupId}/posts`, { content });
  invalidateApiGetCache([`/communities/${groupId}/posts`]);
  return post;
}
