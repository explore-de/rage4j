import Heading from '@theme/Heading';
import styles from './styles.module.css';
import Link from "@docusaurus/Link";
import CodeBlock from "@theme/CodeBlock";
import {formatNumber, getGitHubStats} from "@site/src/api/GitHubApi";

const mavenDependency = `<dependency>
    <groupId>de.explore</groupId>
    <artifactId>rage4j</artifactId>
    <version>1.0.1</version>
</dependency>`;

export default function HomepageFeatures(): JSX.Element {
    const {stats, loading} = getGitHubStats('explore-de', 'rage4j');
    const githubStars = stats.stars;
    const githubForks = stats.forks;

    return (
        <>
            <section className={styles.statsSection}>
                <div className="container">
                    <div className={styles.statsGrid}>
                        <div className={styles.statsCard}>
                            <div className={styles.statsIcon}>⭐</div>
                            <div className={styles.statsValue}>
                                {loading ? '0' : formatNumber(githubStars)}
                            </div>
                            <div className={styles.statsLabel}>GitHub Stars</div>
                        </div>
                        <div className={styles.statsCard}>
                            <div className={styles.statsIcon}>🔄</div>
                            <div className={styles.statsValue}>
                                {loading ? '0' : formatNumber(githubForks)}
                            </div>
                            <div className={styles.statsLabel}>Forks</div>
                        </div>
                        <div className={styles.statsCard}>
                            <div className={styles.statsIcon}>📦</div>
                            <div className={styles.statsValue}>
                                {loading ? '0.0.0' : stats.version}
                            </div>
                            <div className={styles.statsLabel}>Latest Version</div>
                        </div>
                    </div>
                </div>
            </section>
            <section className={styles.featuresSection}>
                <div className="container">
                    <h2 className={styles.featuresTitle}>Key Features</h2>
                    <div className={styles.featureGrid}>
                        <div className={styles.featureCard}>
                            <h3>📊 Comprehensive Evaluation</h3>
                            <p>Measure accuracy, relevance, and faithfulness of AI responses</p>
                        </div>
                        <div className={styles.featureCard}>
                            <h3>⚡ High Performance</h3>
                            <p>Optimized for Java with minimal overhead</p>
                        </div>
                        <div className={styles.featureCard}>
                            <h3>🔌 Easy Integration</h3>
                            <p>Simple Maven dependency with clear APIs</p>
                        </div>
                    </div>
                </div>
            </section>
            <section className={styles.installationSection}>
                <div className="container">
                    <div>
                        <Heading as="h2" className={styles.installationTitle}>Installation</Heading>
                        <p className={styles.installationDescription}>
                            Add the following maven dependency to your project's pom.xml file:
                        </p>
                    </div>
                    <div>
                        <CodeBlock language="xml">{mavenDependency}</CodeBlock>
                    </div>
                </div>

                <div style={{height: '4rem'}}></div>

                <div className={styles.learnMoreSection}>
                    <p className={styles.learnMoreDescription}>
                        Want to learn more about Rage4J and it's features?
                    </p>
                    <Link
                        className={`button button--secondary button--lg ${styles.getStartedButton}`}
                        to="/docs/intro">
                        Visit Get Started →
                    </Link>
                </div>
                <div style={{height: '4rem'}}></div>
            </section>
        </>
    );
}