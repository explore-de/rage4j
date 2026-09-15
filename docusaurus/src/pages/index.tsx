import clsx from 'clsx';
import Link from '@docusaurus/Link';
import Layout from '@theme/Layout';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import CodeBlock from '@theme/CodeBlock';
import {Highlight, type PrismTheme} from 'prism-react-renderer';
import ThemeToggleHandler from '@site/src/components/ThemeToggleHandler';
import {formatNumber, getGitHubStats} from '@site/src/api/GitHubApi';

import styles from './index.module.css';
import React, {JSX, useEffect, useState} from 'react';

const REPO = 'https://github.com/explore-de/rage4j';
const MAVEN_METADATA = 'https://repo1.maven.org/maven2/dev/rage4j/rage4j/maven-metadata.xml';

const BADGES = [
    {
        alt: 'License: MIT',
        src: 'https://img.shields.io/github/license/explore-de/rage4j?color=brightgreen',
        href: `${REPO}/blob/main/LICENSE`,
    },
    {
        alt: 'Latest release on Maven Central',
        src: `https://img.shields.io/maven-metadata/v?metadataUrl=${encodeURIComponent(MAVEN_METADATA)}&label=maven%20central&color=brightgreen`,
        href: `${REPO}/releases/latest`,
    },
    {
        alt: 'Java 21+',
        src: 'https://img.shields.io/badge/java-21%2B-brightgreen?logo=openjdk&logoColor=white',
        href: 'https://openjdk.org/projects/jdk/21/',
    },
    {
        alt: 'Build status',
        src: 'https://img.shields.io/github/actions/workflow/status/explore-de/rage4j/build.yml?branch=main&label=build',
        href: `${REPO}/actions/workflows/build.yml`,
    },
];

const HERO_CODE = `@Test
void answersFromTheHandbook() {
    rageAssert.given()
        .question("How many vacation days do I get?")
        .groundTruth("You get 30 vacation days per year.")
        .context(handbook)
        .when()
        .answer(assistant::chat)
        .then()
        .assertFaithfulness(0.8)
        .then()
        .assertAnswerCorrectness(0.7);
}`;

const EDITOR_THEME: PrismTheme = {
    plain: {color: '#e8e3e3', backgroundColor: 'transparent'},
    styles: [
        {types: ['comment'], style: {color: '#7a6f6f', fontStyle: 'italic'}},
        {types: ['keyword', 'annotation', 'builtin'], style: {color: '#ff6b6b'}},
        {types: ['string', 'char'], style: {color: '#ffc4a3'}},
        {types: ['number', 'boolean', 'constant'], style: {color: '#ffd479'}},
        {types: ['function'], style: {color: '#f5f5f5'}},
        {types: ['class-name'], style: {color: '#ff9e80'}},
        {types: ['punctuation', 'operator'], style: {color: '#9d9191'}},
    ],
};

const RESULTS = [
    {metric: 'Faithfulness', score: 0.92, threshold: 0.8},
    {metric: 'Answer correctness', score: 0.85, threshold: 0.7},
];

type Metric = { name: string; description: string; kind: 'LLM judge' | 'Embeddings' | 'Deterministic'; to: string };

const METRICS: Metric[] = [
    {
        name: 'Answer Correctness',
        description: 'Compares the claims in an answer against your ground truth.',
        kind: 'LLM judge',
        to: '/docs/rage4j-core/metrics/answer_correctness',
    },
    {
        name: 'Faithfulness',
        description: 'Catches hallucinations: is every claim backed by the retrieved context?',
        kind: 'LLM judge',
        to: '/docs/rage4j-core/metrics/faithfulness',
    },
    {
        name: 'Answer Relevance',
        description: 'Does the answer actually address the question that was asked?',
        kind: 'LLM judge',
        to: '/docs/rage4j-core/metrics/answer_relevance',
    },
    {
        name: 'Semantic Similarity',
        description: 'Embedding distance between the answer and a reference answer.',
        kind: 'Embeddings',
        to: '/docs/rage4j-core/metrics/answer_semantic_similarity',
    },
    {
        name: 'Tool Call Accuracy',
        description: 'Verifies your agent called the right tools with the right arguments.',
        kind: 'Deterministic',
        to: '/docs/rage4j-core/metrics/tool_call_accuracy',
    },
    {
        name: 'BLEU & ROUGE',
        description: 'Classic n-gram and LCS overlap scores, fast and free to run.',
        kind: 'Deterministic',
        to: '/docs/rage4j-core/metrics/bleu_score',
    },
];

const STEPS = [
    {
        keyword: 'given()',
        title: 'Describe the case',
        text: 'A question, the answer you expect, and the context your retriever should find.',
    },
    {
        keyword: 'when()',
        title: 'Call your app',
        text: 'Pass any function: a LangChain4j AI service, a REST client, your own pipeline.',
    },
    {
        keyword: 'then()',
        title: 'Assert on quality',
        text: 'Set score thresholds per metric. A regression fails the build like any other test.',
    },
];

const MODULES = [
    {
        artifact: 'rage4j',
        title: 'Core',
        text: 'Evaluators, samples and aggregation. Use it anywhere, not only in tests.',
        to: '/docs/category/rage4j-core',
    },
    {
        artifact: 'rage4j-assert',
        title: 'Assert',
        text: 'The fluent given / when / then API for LLM assertions in your test suite.',
        to: '/docs/category/rage4j-assert',
    },
    {
        artifact: 'rage4j-persist',
        title: 'Persist',
        text: 'Write every evaluation to JSON Lines to track quality across runs.',
        to: '/docs/rage4j-persist/introduction',
    },
    {
        artifact: 'rage4j-persist-junit5',
        title: 'Persist JUnit 5',
        text: 'One annotation manages the store lifecycle for a whole test class.',
        to: '/docs/rage4j-persist-junit5/introduction',
    },
];

function Badges() {
    return (
        <div className={styles.badges}>
            {BADGES.map(badge => (
                <a key={badge.alt} href={badge.href} target="_blank" rel="noopener noreferrer">
                    <img src={badge.src} alt={badge.alt} height={20}/>
                </a>
            ))}
        </div>
    );
}

function prefersReducedMotion() {
    return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

function useTypewriter(text: string, speed: number, delay: number) {
    const [length, setLength] = useState(0);

    useEffect(() => {
        if (prefersReducedMotion()) {
            setLength(text.length);
            return;
        }
        let interval: ReturnType<typeof setInterval>;
        const timeout = setTimeout(() => {
            interval = setInterval(() => {
                setLength(current => {
                    if (current >= text.length) {
                        clearInterval(interval);
                        return current;
                    }
                    return current + 1;
                });
            }, speed);
        }, delay);
        return () => {
            clearTimeout(timeout);
            clearInterval(interval);
        };
    }, [text, speed, delay]);

    return {typed: text.slice(0, length), done: length >= text.length};
}

function revealDelay(ms: number) {
    return {'--reveal-delay': `${ms}ms`} as React.CSSProperties;
}

function useRevealOnScroll() {
    useEffect(() => {
        const elements = document.querySelectorAll(`.${styles.reveal}`);
        if (prefersReducedMotion()) {
            elements.forEach(element => element.classList.add(styles.revealed));
            return;
        }
        const observer = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add(styles.revealed);
                    observer.unobserve(entry.target);
                }
            });
        }, {threshold: 0.15, rootMargin: '0px 0px -40px 0px'});
        elements.forEach(element => observer.observe(element));
        return () => observer.disconnect();
    }, []);
}

function Editor() {
    const {typed, done} = useTypewriter(HERO_CODE, 22, 900);

    return (
        <div className={styles.editor}>
            <div className={styles.editorBar}>
                <span className={styles.dot}/>
                <span className={styles.dot}/>
                <span className={styles.dot}/>
                <span className={styles.editorFile}>HandbookAssistantTest.java</span>
            </div>
            <Highlight code={typed} language="java" theme={EDITOR_THEME}>
                {({tokens, getLineProps, getTokenProps}) => (
                    <pre className={styles.editorCode}>
                        {tokens.map((line, i) => (
                            <div key={i} {...getLineProps({line})}>
                                <span className={styles.lineNumber}>{i + 1}</span>
                                {line.map((token, key) => (
                                    <span key={key} {...getTokenProps({token})}/>
                                ))}
                                {!done && i === tokens.length - 1 && <span className={styles.caret}/>}
                            </div>
                        ))}
                    </pre>
                )}
            </Highlight>
            <div className={clsx(styles.testRun, done && styles.testRunDone)}>
                {RESULTS.map(result => (
                    <div key={result.metric} className={styles.testResult}>
                        <span className={styles.check}>{done ? '✓' : '○'}</span>
                        <span className={styles.testMetric}>{result.metric}</span>
                        <span className={styles.scoreBar}>
                            <span style={{width: `${result.score * 100}%`}}/>
                            <i style={{left: `${result.threshold * 100}%`}}/>
                        </span>
                        <span className={styles.score}>{done ? result.score.toFixed(2) : '…'}</span>
                    </div>
                ))}
                <div className={styles.summary}>
                    <span className={styles.summaryCheck}>
                        <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
                            <path d="M5 12.5l4.5 4.5L19 7.5" fill="none" stroke="currentColor" strokeWidth="3"
                                  strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                    </span>
                    {done ? `${RESULTS.length} assertions passed` : 'Running…'}
                </div>
            </div>
        </div>
    );
}

function Hero({stars}: { stars: number | null }) {
    const scrollToContent = () => {
        document.querySelector('main')?.scrollIntoView({behavior: 'smooth'});
    };

    return (
        <header className={styles.hero}>
            <div className={clsx(styles.wrap, styles.heroGrid)}>
                <div className={styles.heroText}>
                    <div className={styles.brand}>
                        <span className={styles.logoStage}>
                            <img src="img/rage4j-mark.png" alt="" draggable="false" className={styles.logo}/>
                        </span>
                        <span className={styles.wordmark}>Rage4J</span>
                    </div>
                    <h1 className={styles.heroTitle}>
                        Test your LLM apps like the rest of your <span>Java code</span>
                    </h1>
                    <p className={styles.heroLead}>
                        Evaluate RAG pipelines and AI agents in plain JUnit tests. Assert on correctness,
                        faithfulness, relevance and tool calls, and let CI catch quality regressions.
                    </p>
                    <div className={styles.actions}>
                        <Link className={clsx(styles.btn, styles.btnPrimary)} to="/docs/intro">
                            Get started →
                        </Link>
                        <Link className={clsx(styles.btn, styles.btnGhost)} href={REPO}>
                            <GitHubIcon/>
                            GitHub
                            {stars !== null && <span className={styles.stars}>★ {formatNumber(stars)}</span>}
                        </Link>
                    </div>
                    <Badges/>
                </div>
                <Editor/>
            </div>
            <button className={styles.scrollArrow} onClick={scrollToContent} aria-label="Scroll to content">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path d="M7 10L12 15L17 10" stroke="currentColor" strokeWidth="2" strokeLinecap="round"
                          strokeLinejoin="round"/>
                </svg>
            </button>
        </header>
    );
}

function HowItWorks() {
    return (
        <section className={styles.section}>
            <div className={styles.wrap}>
                <p className={clsx(styles.eyebrow, styles.reveal)}>How it works</p>
                <h2 className={clsx(styles.sectionTitle, styles.reveal)}>The test structure you already know</h2>
                <ol className={styles.steps}>
                    {STEPS.map((step, i) => (
                        <li key={step.keyword} className={clsx(styles.step, styles.reveal)}
                            style={revealDelay(i * 90)}>
                            <span className={styles.stepNumber}>{i + 1}</span>
                            <code className={styles.stepKeyword}>{step.keyword}</code>
                            <h3>{step.title}</h3>
                            <p>{step.text}</p>
                        </li>
                    ))}
                </ol>
            </div>
        </section>
    );
}

function Metrics() {
    return (
        <section className={clsx(styles.section, styles.sectionTinted)}>
            <div className={styles.wrap}>
                <p className={clsx(styles.eyebrow, styles.reveal)}>Metrics</p>
                <h2 className={clsx(styles.sectionTitle, styles.reveal)}>Measure what matters in a RAG answer</h2>
                <div className={styles.metricGrid}>
                    {METRICS.map((metric, i) => (
                        <Link key={metric.name} to={metric.to} className={clsx(styles.metricCard, styles.reveal)}
                              style={revealDelay(i * 70)}>
                            <span className={clsx(styles.kind, styles[`kind${metric.kind.replace(' ', '')}`])}>
                                {metric.kind}
                            </span>
                            <h3>{metric.name}</h3>
                            <p>{metric.description}</p>
                            <span className={styles.more}>Read more →</span>
                        </Link>
                    ))}
                </div>
            </div>
        </section>
    );
}

function Modules() {
    return (
        <section className={styles.section}>
            <div className={styles.wrap}>
                <p className={clsx(styles.eyebrow, styles.reveal)}>Modules</p>
                <h2 className={clsx(styles.sectionTitle, styles.reveal)}>Pick only what you need</h2>
                <div className={styles.moduleGrid}>
                    {MODULES.map((module, i) => (
                        <Link key={module.artifact} to={module.to} className={clsx(styles.moduleCard, styles.reveal)}
                              style={revealDelay(i * 80)}>
                            <code>{module.artifact}</code>
                            <h3>{module.title}</h3>
                            <p>{module.text}</p>
                        </Link>
                    ))}
                </div>
            </div>
        </section>
    );
}

function Install({version}: { version: string }) {
    const maven = `<dependency>
    <groupId>dev.rage4j</groupId>
    <artifactId>rage4j-assert</artifactId>
    <version>${version}</version>
    <scope>test</scope>
</dependency>`;
    const gradle = `testImplementation("dev.rage4j:rage4j-assert:${version}")`;

    return (
        <section className={clsx(styles.section, styles.sectionTinted)}>
            <div className={clsx(styles.wrap, styles.installGrid)}>
                <div>
                    <p className={clsx(styles.eyebrow, styles.reveal)}>Installation</p>
                    <h2 className={clsx(styles.sectionTitle, styles.reveal)}>Add one test dependency</h2>
                    <p className={styles.sectionLead}>
                        Rage4J is on Maven Central. It builds on LangChain4j, so bring the model provider you
                        already use, such as OpenAI or Ollama.
                    </p>
                </div>
                <div className={clsx(styles.installCode, styles.reveal)}>
                    <Tabs groupId="build-tool">
                        <TabItem value="maven" label="Maven" default>
                            <CodeBlock language="xml">{maven}</CodeBlock>
                        </TabItem>
                        <TabItem value="gradle" label="Gradle">
                            <CodeBlock language="kotlin">{gradle}</CodeBlock>
                        </TabItem>
                    </Tabs>
                </div>
            </div>
        </section>
    );
}

function CallToAction() {
    return (
        <section className={styles.section}>
            <div className={clsx(styles.wrap, styles.cta, styles.reveal)}>
                <h2>Stop eyeballing LLM output.</h2>
                <p>Write your first evaluation test in five minutes.</p>
                <div className={styles.actions}>
                    <Link className={clsx(styles.btn, styles.btnLight)} to="/docs/intro">
                        Read the docs →
                    </Link>
                    <Link className={clsx(styles.btn, styles.btnOutlineLight)} to="/docs/rage4j-assert/examples">
                        See examples
                    </Link>
                </div>
            </div>
        </section>
    );
}

function GitHubIcon() {
    return (
        <svg width="18" height="18" viewBox="0 0 16 16" fill="currentColor" aria-hidden="true">
            <path
                d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.013 8.013 0 0016 8c0-4.42-3.58-8-8-8z"/>
        </svg>
    );
}

export default function Home(): JSX.Element {
    const {stats, loading} = getGitHubStats('explore-de', 'rage4j');
    useRevealOnScroll();

    return (
        <Layout
            title="Test your LLM apps like the rest of your Java code"
            description="Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.">
            <ThemeToggleHandler/>
            <Hero stars={loading ? null : stats.stars}/>
            <main>
                <HowItWorks/>
                <Metrics/>
                <Modules/>
                <Install version={stats.version}/>
                <CallToAction/>
            </main>
        </Layout>
    );
}
