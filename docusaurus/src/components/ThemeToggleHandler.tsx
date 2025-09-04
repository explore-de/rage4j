import React from 'react';
import type {ColorMode} from '@docusaurus/theme-common';
import {useColorMode} from '@docusaurus/theme-common';

const ThemeToggleHandler: React.FC = () => {
    const {colorMode, setColorMode} = useColorMode();

    const isMobile = () => {
        return window.innerWidth <= 768 || /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
    };

    React.useEffect(() => {
        let overlay = document.getElementById('theme-wall-overlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'theme-wall-overlay';
            overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100vw;
        height: 100vh;
        z-index: 99999;
        pointer-events: none;
        opacity: 0;
        background: #000000;
        transform: translateX(-100%);
      `;
            document.body.appendChild(overlay);
        }

        const animateThemeSwitch = (newTheme: ColorMode) => {
            if (isMobile()) {
                setColorMode(newTheme);
                return;
            }

            const overlay = document.getElementById('theme-wall-overlay');
            if (!overlay) return;

            overlay.style.background = newTheme === 'light' ? '#ffffff' : '#000000';
            overlay.style.opacity = '1';

            overlay.style.transform = 'translateX(-100%)';
            overlay.style.transition = 'transform 300ms cubic-bezier(0.25, 0.46, 0.45, 0.94)';

            requestAnimationFrame(() => {
                overlay.style.transform = 'translateX(0%)';

                setTimeout(() => {
                    setColorMode(newTheme);

                    setTimeout(() => {
                        requestAnimationFrame(() => {
                            requestAnimationFrame(() => {
                                overlay.style.transform = 'translateX(100%)';

                                setTimeout(() => {
                                    overlay.style.opacity = '0';
                                    overlay.style.transition = 'none';
                                    overlay.style.transform = 'translateX(-100%)';
                                }, 300);
                            });
                        });
                    }, 250);
                }, 300);
            });
        };

        const interceptClick = (e: Event) => {
            const target = e.target as HTMLElement;

            const isThemeToggle = target.closest('button') && (
                target.closest('[aria-label*="Switch between dark and light mode"]') ||
                target.closest('[title*="Switch between dark and light mode"]') ||
                target.closest('[aria-label*="Switch theme"]') ||
                target.closest('[title*="Switch theme"]') ||
                target.closest('.toggle') ||
                target.closest('[data-theme-toggle]') ||
                (target.textContent?.toLowerCase().includes('theme') && !target.closest('.navbar__item')) ||
                (target.textContent?.toLowerCase().includes('dark') && !target.closest('.navbar__item')) ||
                (target.textContent?.toLowerCase().includes('light') && !target.closest('.navbar__item'))
            );

            if (isThemeToggle) {
                e.preventDefault();
                e.stopPropagation();

                const newTheme: ColorMode = colorMode === 'dark' ? 'light' : 'dark';
                animateThemeSwitch(newTheme);
            }
        };

        document.addEventListener('click', interceptClick, true);

        return () => {
            document.removeEventListener('click', interceptClick, true);
        };
    }, [colorMode, setColorMode]);

    return null;
};

export default ThemeToggleHandler;
