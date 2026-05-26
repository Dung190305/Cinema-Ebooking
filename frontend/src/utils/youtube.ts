// utils/youtube.ts
export function getYouTubeIdFromUrl(url: string): string | null {
  if (!url) return null;
  const regex = /(?:youtube\.com\/watch\?v=|youtu\.be\/)([^&\n?#]+)/;
  const match = url.match(regex);
  return match ? match[1] : null;
}

export function getYouTubeThumbnailUrl(videoId: string, quality: 'default' | 'mq' | 'hq' | 'sd' | 'maxres' = 'maxres'): string {
  const qualities = {
    default: 'default.jpg',      // 120x90
    mq: 'mqdefault.jpg',        // 320x180
    hq: 'hqdefault.jpg',        // 480x360
    sd: 'sddefault.jpg',        // 640x480
    maxres: 'maxresdefault.jpg' // 1280x720
  };
  return `https://img.youtube.com/vi/${videoId}/${qualities[quality]}`;
}