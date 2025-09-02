import React, {useEffect, useState} from 'react';
import Heading from '@theme/Heading';
import styles from './styles.module.css';
import Link from "@docusaurus/Link";
import CodeBlock from "@theme/CodeBlock";
import {formatNumber, getGitHubStats} from "@site/src/api/GitHubApi";

const getMavenDependency = (version: string) => `<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j</artifactId>
    <version>${version}</version>
</dependency>`;

const getAssertDependency = (version: string) => `<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-assert</artifactId>
    <version>${version}</version>
</dependency>`;

const useScrollAnimation = () => {
    const [visibleElements, setVisibleElements] = useState(new Set<string>());

    useEffect(() => {
        const handleIntersection = (entries: IntersectionObserverEntry[]) => {
            entries.forEach((entry) => {
                if (entry.isIntersecting) {
                    setVisibleElements(prev => {
                        const newSet = new Set(prev);
                        newSet.add(entry.target.id);
                        return newSet;
                    });
                }
            });
        };

        const observer = new IntersectionObserver(handleIntersection, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });

        const elements = document.querySelectorAll('.scroll-animate');
        elements.forEach((el) => observer.observe(el));

        return () => observer.disconnect();
    }, []);

    return visibleElements;
};

export default function HomepageFeatures(): React.JSX.Element {
    const {stats, loading} = getGitHubStats('explore-de', 'rage4j');
    const githubStars = stats.stars;
    const githubForks = stats.forks;
    const visibleElements = useScrollAnimation();

    return (
        <>
            <section
                id="stats-section"
                className={`${styles.statsSection} scroll-animate ${visibleElements.has('stats-section') ? styles.visible : ''}`}
            >
                <div className="container">
                    <div className={styles.statsGrid}>
                        <div className={`${styles.statsCard} ${styles.animateCard1}`}>
                            <div className={styles.statsIcon}>⭐</div>
                            <div className={styles.statsValue}>
                                {loading ? '0' : formatNumber(githubStars)}
                            </div>
                            <div className={styles.statsLabel}>GitHub Stars</div>
                        </div>
                        <div className={`${styles.statsCard} ${styles.animateCard2}`}>
                            <div className={styles.statsIcon}>🔄</div>
                            <div className={styles.statsValue}>
                                {loading ? '0' : formatNumber(githubForks)}
                            </div>
                            <div className={styles.statsLabel}>Forks</div>
                        </div>
                        <div className={`${styles.statsCard} ${styles.animateCard3}`}>
                            <div className={styles.statsIcon}>📦</div>
                            <div className={styles.statsValue}>
                                {loading ? '0.0.0' : stats.version}
                            </div>
                            <div className={styles.statsLabel}>Latest Version</div>
                        </div>
                    </div>
                </div>
            </section>

            <div className={styles.sectionDivider}></div>

            <section
                id="features-section"
                className={`${styles.features} scroll-animate ${visibleElements.has('features-section') ? styles.visible : ''}`}
            >
                <div className="container">
                    <div className="text--center">
                        <Heading as="h2" className={styles.featuresTitle}>
                            Key Features
                        </Heading>
                    </div>
                    <div className="row">
                        <div
                            className={`col col--4 ${styles.featureCard} scroll-animate ${visibleElements.has('feature-1') ? styles.visible : ''}`}
                            id="feature-1">
                            <div className="text--center">
                                <Heading as="h3">📊 Comprehensive Evaluation</Heading>
                                <p>
                                    Measure accuracy, relevance, and faithfulness of AI responses
                                </p>
                            </div>
                        </div>
                        <div
                            className={`col col--4 ${styles.featureCard} scroll-animate ${visibleElements.has('feature-2') ? styles.visible : ''}`}
                            id="feature-2">
                            <div className="text--center">
                                <Heading as="h3">⚡ High Performance</Heading>
                                <p>
                                    Optimized for Java with minimal overhead
                                </p>
                            </div>
                        </div>
                        <div
                            className={`col col--4 ${styles.featureCard} scroll-animate ${visibleElements.has('feature-3') ? styles.visible : ''}`}
                            id="feature-3">
                            <div className="text--center">
                                <Heading as="h3">🔌 Easy Integration</Heading>
                                <p>
                                    Simple Maven dependency with clear APIs
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <div className={styles.sectionDivider}></div>

            <section
                id="installation-section"
                className={`${styles.installationSection} scroll-animate ${visibleElements.has('installation-section') ? styles.visible : ''}`}
            >
                <div className="container">
                    <div className="text--center">
                        <Heading as="h2" className={styles.installationTitle}>
                            Installation
                        </Heading>
                    </div>
                    <div className="row">
                        <div className="col col--12">
                            <div className={styles.installationCard}>
                                <p className={styles.alternativeText}>
                                    Get started by adding the RAGE4J dependency to your project:
                                </p>
                                <CodeBlock language="xml">
                                    {getMavenDependency(stats.version)}
                                </CodeBlock>
                            </div>
                        </div>
                    </div>
                    <div className="row" style={{marginTop: '0rem'}}>
                        <div className="col col--12">
                            <div className={styles.installationCard}>
                                <p className={styles.alternativeText}>
                                    Or try our intuitive and user-friendly wrapper for the RAGE4J-Core API:
                                </p>
                                <CodeBlock language="xml">
                                    {getAssertDependency(stats.version)}
                                </CodeBlock>
                            </div>
                        </div>
                    </div>
                    <div className={styles.sectionDivider} style={{marginTop: '3rem'}}></div>
                    <div className={`text--center ${styles.learnMoreSection}`} style={{marginTop: '7rem'}}>
                        <p className={styles.learnMoreText}>
                            Want to learn more about Rage4J and it's features?
                        </p>
                        <Link
                            className="button button--secondary button--lg"
                            to="/docs/intro">
                            Visit Get Started →
                        </Link>
                    </div>
                </div>
            </section>
        </>
    );
}
