import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';
import React from 'react';
import CodeBlock from "@theme/CodeBlock";
import {useTypewriter} from "@site/src/api/GitHubApi";

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
                                // change to actual github repository
                                className="button button--outline button--secondary button--lg"
                                to="https://github.com/exp/rage4j">
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
