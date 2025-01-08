import { themes as prismThemes } from "prism-react-renderer";
import type { Config } from "@docusaurus/types";
import type * as Preset from "@docusaurus/preset-classic";

const config: Config = {
  title: "Rage4J",
  tagline: "Comprehensive RAG Evaluation Library for Java",
  favicon: "img/favicon.ico",

  /* change to actual website */
  url: "https://rage4j.dev",
  baseUrl: "/",

  /* change to actual github repository */
  organizationName: "exp",
  projectName: "rage4j",

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      {
        docs: {
          sidebarPath: "./sidebars.ts",
          editUrl:
            "https://github.com/exp/rage4j" /* change to actual github repository */,
        },
        theme: {
          customCss: "./src/css/custom.css",
        },
      } satisfies Preset.Options,
    ],
  ],

  themeConfig: {
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
          href: "https://github.com/exp/rage4j" /* change to actual github repository */,
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
              href: "https://github.com/exp/rage4j" /* change to actual github repository */,
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
      darkTheme: prismThemes.dracula,
      additionalLanguages: ["java", "scala"],
    },
  } satisfies Preset.ThemeConfig,
};

export default config;
