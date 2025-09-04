import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';
import React, {useEffect, useState} from 'react';
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

    const fiveLiner = `rage4j
     .question("What is the capital of France?")
     .groundTruth("Paris is the capital of France")
     .runQuery(q -> askYourChatModel(q))
     .assertFaithfullness(0.7, 0.1);`;

    const animatedCode = useTypewriter(fiveLiner, 30);

    return (
        <header className={clsx('hero hero--primary', styles.heroSection)}>
            <div className="container">
                <div className={styles.heroContent}>
                    <img
                        src="img/rage4j.png"
                        alt="Rage4J Logo"
                        className={styles.heroLogo}
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
                </div>
            </div>
        </header>
    );
}

export default function Home(): JSX.Element {
    return (
        <Layout>
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}