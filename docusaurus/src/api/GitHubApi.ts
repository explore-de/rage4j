import {useEffect, useState} from 'react';
import Cookies from 'js-cookie';

interface GitHubApi {
    stars: number;
    forks: number;
    version: string;
}

const COOKIE_KEY = 'github_stats';

function getStoredStats(): GitHubApi | null {
    const stored = Cookies.get(COOKIE_KEY);
    if (!stored) return null;

    const data = JSON.parse(stored);
    const now = new Date().getTime();

    if (now > data.expiry) {
        Cookies.remove(COOKIE_KEY);
        return null;
    }

    return data.stats;
}

function storeStats(stats: GitHubApi) {
    const expiry = new Date().getTime() + (6 * 60 * 60 * 1000); // default is 6 hours
    Cookies.set(COOKIE_KEY, JSON.stringify({stats, expiry}), {expires: 6 / 24});
}

export function getGitHubStats(owner: string, repo: string) {
    const [stats, setStats] = useState<GitHubApi>({stars: 0, forks: 0, version: '1.0.4'});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const cachedStats = getStoredStats();
        if (cachedStats) {
            setStats(cachedStats);
            setLoading(false);
            return;
        }

        const fetchOptions = {
            headers: {
                'Accept': 'application/vnd.github.v3+json'
            }
        };

        Promise.all([
            fetch(`https://api.github.com/repos/${owner}/${repo}`, fetchOptions),
            fetch(`https://api.github.com/repos/${owner}/${repo}/releases/latest`, fetchOptions)
        ])
            .then(([repoResponse, releaseResponse]) => {
                if (!repoResponse.ok) throw new Error(`GitHub API Error: ${repoResponse.status}`);
                return Promise.all([repoResponse.json(), releaseResponse.json()]);
            })
            .then(([repoData, releaseData]) => {
                let version = '0.0.0';
                if (releaseData.tag_name) {
                    let tagName = releaseData.tag_name.replace(/^v/i, '');
                    const versionMatch = tagName.match(/\d+\.\d+\.\d+/);
                    if (versionMatch) {
                        version = versionMatch[0];
                    }
                }

                const newStats = {
                    stars: repoData.stargazers_count || 0,
                    forks: repoData.forks_count || 0,
                    version: version
                };
                setStats(newStats);
                storeStats(newStats);
            })
            .catch(console.error)
            .finally(() => setLoading(false));
    }, [owner, repo]);

    return {stats, loading};
}

export function formatNumber(num: number): string {
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
}