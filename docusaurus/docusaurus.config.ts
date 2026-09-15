import {themes as prismThemes} from "prism-react-renderer";
import type {Config} from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
    title: "Rage4J",
    tagline: "Test your LLM apps like the rest of your Java code",
    favicon: "img/favicon.ico",

    url: "https://rage4j.dev",
    baseUrl: "/",

    organizationName: "explore-de",
    projectName: "rage4j",

    trailingSlash: false,

    onBrokenLinks: "throw",

    i18n: {
        defaultLocale: "en",
        locales: ["en"],
    },

    headTags: [
        {
            tagName: 'meta',
            attributes: {
                name: 'description',
                content: 'Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:title',
                content: 'Rage4J - Test your LLM apps like the rest of your Java code',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:description',
                content: 'Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:image',
                content: 'https://rage4j.dev/img/opengraph.png',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:type',
                content: 'website',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:url',
                content: 'https://rage4j.dev/',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:card',
                content: 'summary_large_image',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:title',
                content: 'Rage4J - Test your LLM apps like the rest of your Java code',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:description',
                content: 'Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:image',
                content: 'https://rage4j.dev/img/opengraph.png',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:image:alt',
                content: 'Rage4J - Test your LLM apps like the rest of your Java code',
            },
        },
    ],

    presets: [
        [
            "classic",
            {
                docs: {
                    sidebarPath: "./sidebars.ts",
                    editUrl:
                        "https://github.com/explore-de/rage4j",
                },
                theme: {
                    customCss: "./src/css/custom.css",
                },
            } satisfies Preset.Options,
        ],
    ],

    themeConfig: {
        image: 'img/opengraph.png',
        metadata: [
            {
                name: 'description',
                content: 'Rage4J - Test your LLM apps like the rest of your Java code. Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.'
            },
            {property: 'og:title', content: 'Rage4J - Test your LLM apps like the rest of your Java code'},
            {
                property: 'og:description',
                content: 'Evaluate RAG pipelines and AI agents in plain JUnit tests, with metrics for answer correctness, relevance, faithfulness, tool call accuracy and more.'
            },
            {property: 'og:url', content: 'https://rage4j.dev/'},
            {property: 'og:type', content: 'website'},
            {property: 'og:image', content: 'https://rage4j.dev/img/opengraph.png'},
            {property: 'og:image:alt', content: 'Rage4J - Test your LLM apps like the rest of your Java code'},
            {property: 'og:site_name', content: 'Rage4J'},
            {property: 'og:locale', content: 'en_US'},
            {name: 'twitter:card', content: 'summary_large_image'},
            {name: 'twitter:title', content: 'Rage4J - Test your LLM apps like the rest of your Java code'},
            {
                name: 'twitter:description',
                content: 'Evaluate RAG pipelines and AI agents in plain JUnit tests.'
            },
            {name: 'twitter:image', content: 'https://rage4j.dev/img/opengraph.png'},
            {name: 'twitter:image:alt', content: 'Rage4J - Test your LLM apps like the rest of your Java code'},
            {
                name: 'keywords',
                content: 'RAG, Retrieval Augmented Generation, Java, Evaluation, Metrics, AI, Machine Learning, NLP'
            },
            {name: 'author', content: 'EXP Software GmbH'},
        ],
        navbar: {
            title: "Rage4J",
            logo: {
                alt: "Rage4J logo",
                src: "img/rage4j-mark.png",
            },
            hideOnScroll: false,
            items: [
                {
                    type: "docSidebar",
                    sidebarId: "tutorialSidebar",
                    position: "left",
                    label: "Docs",
                },
                {
                    to: "/docs/rage4j-core/metrics/overview",
                    position: "left",
                    label: "Metrics",
                },
                {
                    to: "/docs/rage4j-assert/examples",
                    position: "left",
                    label: "Examples",
                },
                {
                    href: "https://github.com/explore-de/rage4j",
                    position: "right",
                    className: "header-github-link",
                    "aria-label": "GitHub repository",
                },
            ],
        },
        footer: {
            style: "dark",
            links: [
                {
                    title: "Docs",
                    items: [
                        {label: "Getting Started", to: "/docs/intro"},
                        {label: "Rage4J Core", to: "/docs/category/rage4j-core"},
                        {label: "Rage4J Assert", to: "/docs/category/rage4j-assert"},
                        {label: "Metrics", to: "/docs/rage4j-core/metrics/overview"},
                    ],
                },
                {
                    title: "Project",
                    items: [
                        {label: "Source on GitHub", href: "https://github.com/explore-de/rage4j"},
                        {label: "Issues", href: "https://github.com/explore-de/rage4j/issues"},
                        {label: "Releases", href: "https://github.com/explore-de/rage4j/releases"},
                        {label: "MIT licence", href: "https://github.com/explore-de/rage4j/blob/main/LICENSE"},
                    ],
                },
                {
                    title: "Legal",
                    items: [
                        {label: "Impressum", to: "/impressum"},
                        {label: "Datenschutz", href: "https://explore.de/datenschutz"},
                    ],
                },
            ],
            logo: {
                alt: "EXP Software GmbH",
                src: "img/exp-logo.svg",
                href: "https://explore.de",
                height: 26,
            },
            copyright: `Built and maintained by <strong>EXP Software GmbH</strong> · Ludwig-Hirschberger-Allee 11 · 85276 Pfaffenhofen an der Ilm · Deutschland<br/>© ${new Date().getFullYear()} EXP Software GmbH and the Rage4J contributors · MIT`,
        },
        customFields: {
            githubToken: process.env.GITHUB_TOKEN || "",
        },
        prism: {
            theme: prismThemes.github,
            darkTheme: {
                "plain": {
                    "color": "#F8F8F2"
                },
                "styles": [
                    {
                        "types": [
                            "prolog",
                            "constant",
                            "builtin"
                        ],
                        "style": {
                            "color": "rgb(189, 147, 249)"
                        }
                    },
                    {
                        "types": [
                            "inserted",
                            "function"
                        ],
                        "style": {
                            "color": "rgb(80, 250, 123)"
                        }
                    },
                    {
                        "types": [
                            "deleted"
                        ],
                        "style": {
                            "color": "rgb(255, 85, 85)"
                        }
                    },
                    {
                        "types": [
                            "changed"
                        ],
                        "style": {
                            "color": "rgb(255, 184, 108)"
                        }
                    },
                    {
                        "types": [
                            "punctuation",
                            "symbol"
                        ],
                        "style": {
                            "color": "rgb(248, 248, 242)"
                        }
                    },
                    {
                        "types": [
                            "string",
                            "char",
                            "tag",
                            "selector"
                        ],
                        "style": {
                            "color": "rgb(255, 121, 198)"
                        }
                    },
                    {
                        "types": [
                            "keyword",
                            "variable"
                        ],
                        "style": {
                            "color": "rgb(189, 147, 249)",
                            "fontStyle": "italic"
                        }
                    },
                    {
                        "types": [
                            "comment"
                        ],
                        "style": {
                            "color": "rgb(98, 114, 164)"
                        }
                    },
                    {
                        "types": [
                            "attr-name"
                        ],
                        "style": {
                            "color": "rgb(241, 250, 140)"
                        }
                    }
                ]
            },
            additionalLanguages: ["java", "scala"],
        },
    } satisfies Preset.ThemeConfig,
};

export default config;
