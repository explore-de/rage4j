import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';
import ThemeToggleHandler from '@site/src/components/ThemeToggleHandler';

import styles from './index.module.css';
import React, {JSX, useEffect, useState} from 'react';
import CodeBlock from "@theme/CodeBlock";

const useTypewriter = (text: string, speed: number = 50) => {
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

function HomepageHeader() {
    const [isLoaded, setIsLoaded] = useState(false);
    const [logoPhase, setLogoPhase] = useState('entrance');

    const fiveLiner = `rage4j
     .question("What is the capital of France?")
     .groundTruth("Paris is the capital of France")
     .runQuery(q -> askYourChatModel(q))
     .assertFaithfullness(0.7, 0.1);`;

    const animatedCode = useTypewriter(fiveLiner, 30);

    useEffect(() => {
        const timer = setTimeout(() => setIsLoaded(true), 100);

        const logoTimer = setTimeout(() => {
            setLogoPhase('floating');
        }, 2500);

        return () => {
            clearTimeout(timer);
            clearTimeout(logoTimer);
        };
    }, []);

    const scrollToFeatures = () => {
        const featuresSection = document.querySelector('main');
        if (featuresSection) {
            featuresSection.scrollIntoView({behavior: 'smooth'});
        }
    };

    return (
        <header className={clsx('hero hero--primary', styles.heroSection, styles.hero, {
            [styles.heroLoaded]: isLoaded
        })}>
            <div className="container">
                <div className={styles.heroContent}>
                    <img
                        src="img/rage4j.png"
                        alt="Rage4J Logo"
                        draggable="false"
                        className={clsx(styles.heroLogo, {
                            [styles.logoFloating]: logoPhase === 'floating'
                        })}
                    />
                    <div className={styles.heroText}>
                        <h1 className="heroTitle">Rage4J</h1>
                        <p className="heroDescription">Comprehensive RAG Evaluation Library for Java</p>
                        <div className={styles.buttons}>
                            <Link className="button button--secondary button--lg" to="/docs/intro">
                                Get Started →
                            </Link>
                            <Link
                                className="button button--outline button--secondary button--lg"
                                to="https://github.com/explore-de/rage4j">
                                View on GitHub
                            </Link>
                        </div>
                    </div>
                </div>
                <div className={styles.heroCodeSection}>
                    <div className={styles.typingCodeWrapper}>
                        <CodeBlock language="java">
                            {animatedCode}
                        </CodeBlock>
                    </div>
                    <div className={styles.scrollArrowContainer}>
                        <button
                            className={styles.scrollArrow}
                            onClick={scrollToFeatures}
                            aria-label="Scroll to features"
                        >
                            <svg
                                width="24"
                                height="24"
                                viewBox="0 0 24 24"
                                fill="none"
                                xmlns="http://www.w3.org/2000/svg"
                            >
                                <path
                                    d="M7 10L12 15L17 10"
                                    stroke="currentColor"
                                    strokeWidth="2"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                />
                            </svg>
                        </button>
                    </div>
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    return (
        <Layout>
            <ThemeToggleHandler/>
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}