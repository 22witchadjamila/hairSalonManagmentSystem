import { api } from './client'

/** One hit from the header search. The server decides what each role may see. */
export interface SearchResult {
  type: string
  id: string
  title: string
  subtitle: string
  link: string
  /** Relevance - results already arrive best-first. */
  score: number
}

export const searchApi = {
  search: (q: string) => api.get<SearchResult[]>(`/search?q=${encodeURIComponent(q)}`),
}
