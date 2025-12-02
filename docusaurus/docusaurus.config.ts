import {themes as prismThemes} from "prism-react-renderer";
import type {Config} from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
    title: "Rage4J",
    tagline: "Comprehensive RAG Evaluation Library for Java",
    favicon: "img/favicon.ico",

    url: "https://explore-de.github.io",
    baseUrl: "/",

    organizationName: "explore-de",
    projectName: "rage4j",

    trailingSlash: false,

    onBrokenLinks: "throw",

    markdown: {
      hooks: {
          onBrokenMarkdownLinks: "warn",
      }
    },

    i18n: {
        defaultLocale: "en",
        locales: ["en"],
    },

    headTags: [
        {
            tagName: 'meta',
            attributes: {
                name: 'description',
                content: 'Evaluate RAG systems with multiple metrics including answer correctness, relevance, faithfulness and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:title',
                content: 'Rage4J - RAG Evaluation Library for Java',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:description',
                content: 'Evaluate RAG systems with multiple metrics including answer correctness, relevance, faithfulness and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                property: 'og:image',
                content: 'https://explore-de.github.io/rage4j/img/opengraph.png',
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
                content: 'https://explore-de.github.io/rage4j/',
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
                content: 'Rage4J - RAG Evaluation Library for Java',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:description',
                content: 'Evaluate RAG systems with multiple metrics including answer correctness, relevance, faithfulness and more.',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:image',
                content: 'https://explore-de.github.io/rage4j/img/opengraph.png',
            },
        },
        {
            tagName: 'meta',
            attributes: {
                name: 'twitter:image:alt',
                content: 'Rage4J - RAG Evaluation Library for Java',
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
                content: 'Rage4J - Comprehensive RAG Evaluation Library for Java. Evaluate your Retrieval-Augmented Generation systems with multiple metrics including answer correctness, relevance, faithfulness and more.'
            },
            {property: 'og:title', content: 'Rage4J - RAG Evaluation Library for Java'},
            {
                property: 'og:description',
                content: 'Comprehensive RAG Evaluation Library for Java. Evaluate your Retrieval-Augmented Generation systems with multiple metrics including answer correctness, relevance, faithfulness and more.'
            },
            {property: 'og:url', content: 'https://explore-de.github.io/rage4j/'},
            {property: 'og:type', content: 'website'},
            {property: 'og:image', content: 'https://explore-de.github.io/rage4j/img/rage4j.png'},
            {property: 'og:image:alt', content: 'Rage4J Logo - RAG Evaluation Library for Java'},
            {property: 'og:site_name', content: 'Rage4J'},
            {property: 'og:locale', content: 'en_US'},
            {name: 'twitter:card', content: 'summary_large_image'},
            {name: 'twitter:title', content: 'Rage4J - RAG Evaluation Library for Java'},
            {
                name: 'twitter:description',
                content: 'Comprehensive RAG Evaluation Library for Java. Evaluate your Retrieval-Augmented Generation systems with multiple metrics.'
            },
            {name: 'twitter:image', content: 'https://explore-de.github.io/rage4j/img/rage4j.png'},
            {name: 'twitter:image:alt', content: 'Rage4J Logo - RAG Evaluation Library for Java'},
            {
                name: 'keywords',
                content: 'RAG, Retrieval Augmented Generation, Java, Evaluation, Metrics, AI, Machine Learning, NLP'
            },
            {name: 'author', content: 'EXP Software GmbH'},
        ],
        navbar: {
            items: [
                {
                    type: "docSidebar",
                    sidebarId: "tutorialSidebar",
                    href: "/",
                    position: "left",
                    label: "Home",
                },
                {
                    type: "docSidebar",
                    sidebarId: "tutorialSidebar",
                    position: "left",
                    label: "Wiki",
                },
                {
                    href: "https://github.com/explore-de/rage4j",
                    label: "GitHub",
                    position: "right",
                },
            ],
        },
        footer: {
            style: "dark",
            links: [
                {
                    title: "Wiki",
                    items: [
                        {
                            label: "Getting Started",
                            to: "/docs/intro",
                        },
                        {
                            label: "RAGE4j-Core",
                            to: "/docs/category/rage4j-core",
                        },
                        {
                            label: "RAGE4j-Assert",
                            to: "/docs/category/rage4j-assert",
                        },
                    ],
                },
                {
                    title: "Open Source",
                    items: [
                        {
                            label: "GitHub",
                            href: "https://github.com/explore-de/rage4j",
                        },
                    ],
                },
            ],
            copyright: `Copyright © ${new Date().getFullYear()} Rage4J made by EXP Software GmbH`,
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
