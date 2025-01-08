import {useEffect, useState} from 'react';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

interface GitHubApi {
    stars: number;
    forks: number;
    version: string;
}

export function getGitHubStats(owner: string, repo: string) {
    const {siteConfig} = useDocusaurusContext();
    const githubToken = siteConfig.customFields.githubToken as string;
    const [stats, setStats] = useState<GitHubApi>({stars: 0, forks: 0, version: '0.0.0'});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchOptions = {
            headers: {
                'Accept': 'application/vnd.github.v3+json',
                ...(githubToken && {
                    'Authorization': `Bearer ${githubToken}`
                })
            },
            method: 'GET'
        };

        Promise.all([
            fetch(`https://api.github.com/repos/${owner}/${repo}`, fetchOptions),
            fetch(`https://api.github.com/repos/${owner}/${repo}/releases/latest`, fetchOptions)
        ])
            .then(([repoResponse, releaseResponse]) => {
                if (!repoResponse.ok || !releaseResponse.ok) {
                    throw new Error('Failed to fetch data');
                }
                return Promise.all([repoResponse.json(), releaseResponse.json()]);
            })
            .then(([repoData, releaseData]) => {
                setStats({
                    stars: repoData.stargazers_count || 0,
                    forks: repoData.forks_count || 0,
                    version: releaseData.tag_name?.replace('v', '') || '0.0.0'
                });
            })
            .catch((error) => {
                console.error('GitHub API Error:', error);
                setStats({stars: 0, forks: 0, version: '0.0.0'});
            })
            .finally(() => {
                setLoading(false);
            });
    }, [owner, repo, githubToken]);

    return {stats, loading};
}

export function formatNumber(num: number): string {
    if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
    }
    return num.toString();
}

export const useTypewriter = (text: string, speed: number = 50) => {
    const [displayText, setDisplayText] = useState('');

    useEffect(() => {
        setDisplayText('');

        const sleep = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

        const typeText = async () => {
            for (let i = 0; i < text.length; i++) {
                await sleep(speed);
                setDisplayText(current => current + text.charAt(i));
            }
        };

        typeText();

        return () => {
            setDisplayText('');
        };
    }, [text, speed]);

    return displayText;
};