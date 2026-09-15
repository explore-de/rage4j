import Link from '@docusaurus/Link';
import Head from '@docusaurus/Head';
import Layout from '@theme/Layout';
import React, {JSX} from 'react';

import styles from './impressum.module.css';

/**
 * EXP Software GmbH, the Diensteanbieter for this site. Figures below are the
 * § 5 TMG mandatory particulars as published at https://explore.de/impressum.
 */
const COMPANY = {
    name: 'EXP Software GmbH',
    street: 'Ludwig-Hirschberger-Allee 11',
    postcode: '85276',
    city: 'Pfaffenhofen an der Ilm',
    country: 'Deutschland',
    phone: '+49 8441 47247-0',
    phoneHref: '+4984414724770',
    fax: '+49 8441 47247-99',
    email: 'kontakt@explore.de',
    site: 'https://explore.de',
    siteLabel: 'explore.de',
    managingDirector: 'Marko Hirsch',
    court: 'Ingolstadt',
    registerNumber: 'HRB 11396',
    vatId: 'DE 363 269 547',
    impressumUrl: 'https://explore.de/impressum',
    privacyUrl: 'https://explore.de/datenschutz',
};

const REPO = 'https://github.com/explore-de/rage4j';

function External({href, children}: { href: string; children: React.ReactNode }) {
    return <a href={href} target="_blank" rel="noreferrer noopener">{children}</a>;
}

export default function Impressum(): JSX.Element {
    return (
        <Layout
            title="Impressum"
            description={`Impressum und Anbieterkennzeichnung nach § 5 TMG für rage4j.dev, ${COMPANY.name}.`}>
            <Head>
                <meta name="robots" content="noindex, follow"/>
                <html lang="de"/>
            </Head>
            <main className={styles.page}>
                <div className={styles.inner}>
                    <header className={styles.head}>
                        <p className={styles.eyebrow}>Anbieterkennzeichnung nach § 5 TMG</p>
                        <h1>Impressum</h1>
                        <p className={styles.lede}>
                            rage4j.dev ist die Projektseite von Rage4J, einer quelloffenen Bibliothek
                            der {COMPANY.name}.
                        </p>
                    </header>

                    <div className={styles.plate}>
                        <a href={COMPANY.site} target="_blank" rel="noreferrer noopener" className={styles.mark}>
                            <img src="/img/exp-logo.svg" alt={COMPANY.name} height={34}/>
                        </a>

                        <dl>
                            <div className={styles.row}>
                                <dt>Diensteanbieter</dt>
                                <dd>
                                    <address>
                                        <strong>{COMPANY.name}</strong><br/>
                                        {COMPANY.street}<br/>
                                        {COMPANY.postcode} {COMPANY.city}<br/>
                                        {COMPANY.country}
                                    </address>
                                </dd>
                            </div>

                            <div className={styles.row}>
                                <dt>Kontakt</dt>
                                <dd>
                                    Telefon: <a href={`tel:${COMPANY.phoneHref}`}>{COMPANY.phone}</a><br/>
                                    Telefax: {COMPANY.fax}<br/>
                                    E-Mail: <a href={`mailto:${COMPANY.email}`}>{COMPANY.email}</a><br/>
                                    Web: <External href={COMPANY.site}>{COMPANY.siteLabel}</External>
                                </dd>
                            </div>

                            <div className={styles.row}>
                                <dt>Vertreten durch</dt>
                                <dd>Geschäftsführer: {COMPANY.managingDirector}</dd>
                            </div>

                            <div className={styles.row}>
                                <dt>Registereintrag</dt>
                                <dd>
                                    Eingetragen im Handelsregister.<br/>
                                    Registergericht: {COMPANY.court}<br/>
                                    Registernummer: {COMPANY.registerNumber}
                                </dd>
                            </div>

                            <div className={styles.row}>
                                <dt>Umsatzsteuer-ID</dt>
                                <dd>
                                    Umsatzsteuer-Identifikationsnummer nach § 27a Umsatzsteuergesetz:<br/>
                                    {COMPANY.vatId}
                                </dd>
                            </div>

                            <div className={styles.row}>
                                <dt>Inhaltlich verantwortlich</dt>
                                <dd>
                                    Nach § 18 Abs. 2 MStV:<br/>
                                    {COMPANY.managingDirector}<br/>
                                    {COMPANY.street}, {COMPANY.postcode} {COMPANY.city}
                                </dd>
                            </div>

                            <div className={styles.row}>
                                <dt>EU-Streitschlichtung</dt>
                                <dd>
                                    Wir sind nicht bereit oder verpflichtet, an Streitbeilegungsverfahren vor einer
                                    Verbraucherschlichtungsstelle teilzunehmen.
                                </dd>
                            </div>
                        </dl>
                    </div>

                    <div className={styles.notes}>
                        <div>
                            <p className={styles.eyebrow}>Haftung und Urheberrecht</p>
                            <p>
                                Es gelten die Haftungs- und Urheberrechtshinweise im{' '}
                                <External href={COMPANY.impressumUrl}>Impressum der {COMPANY.name}</External>.
                            </p>
                        </div>
                        <div>
                            <p className={styles.eyebrow}>Datenschutz</p>
                            <p>
                                Diese Seite wird über GitHub Pages ausgeliefert und verwendet keine
                                Analysewerkzeuge. Ihr Browser lädt Status-Badges von shields.io und fragt
                                Sterne und aktuelle Version bei der GitHub-API ab; dabei erhalten diese
                                Anbieter Ihre IP-Adresse. Das Ergebnis wird sechs Stunden lang im Cookie{' '}
                                <code>github_stats</code> zwischengespeichert. Im Übrigen gilt die{' '}
                                <External href={COMPANY.privacyUrl}>Datenschutzerklärung
                                    der {COMPANY.name}</External>.
                            </p>
                        </div>
                        <div>
                            <p className={styles.eyebrow}>Software</p>
                            <p>
                                Rage4J selbst steht unter der{' '}
                                <External href={`${REPO}/blob/main/LICENSE`}>MIT-Lizenz</External> und wird
                                auf <External href={REPO}>GitHub</External> entwickelt.
                            </p>
                        </div>
                    </div>

                    <Link className={styles.back} to="/">← Zurück zur Startseite</Link>
                </div>
            </main>
        </Layout>
    );
}
