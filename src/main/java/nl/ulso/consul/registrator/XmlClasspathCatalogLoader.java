package nl.ulso.consul.registrator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Creates a {@link Catalog} by parsing an XML file on the classpath.
 * <p>
 * This class has very limited error checking, on purpose:
 * </p>
 * <ul>
 * <li>Within the XML only the elements and attributes known by this agent are accessed. Anything else is ignored.</li>
 * <li>Validation of attribute values is in the {@link nl.ulso.consul.registrator.Catalog.Builder} class</li>
 * </ul>
 */
class XmlClasspathCatalogLoader implements CatalogLoader {

    private static final String DEFAULT_CATALOG_LOCATION = "META-INF/consul-catalog.xml";
    private static final String CATALOG_ELEMENT = "catalog";
    private static final String CATALOG_DELAY = "delay";
    private static final String SERVICE_ELEMENT = "service";
    private static final String SERVICE_NAME = "name";
    private static final String SERVICE_ID = "id";
    private static final String SERVICE_ADDRESS = "address";
    private static final String SERVICE_PORT = "port";
    private static final String HTTP_CHECK_ELEMENT = "http-check";
    private static final String HTTP_CHECK_URL = "url";
    private static final String HTTP_CHECK_INTERVAL = "interval";
    private static final String TAG_ELEMENT = "tag";
    private static final String TAG_NAME = "name";
    private static final String KEY_ELEMENT = "key";
    private static final String KEY_NAME = "name";
    private static final String KEY_VALUE = "value";

    private final String classpathResource;

    XmlClasspathCatalogLoader(final String classpathResource) {
        this.classpathResource = classpathResource;
    }

    XmlClasspathCatalogLoader() {
        this(DEFAULT_CATALOG_LOCATION);
    }

    @Override
    public Catalog load() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(classpathResource)) {
            if (inputStream == null) {
                throw new RegistratorException("No catalog found file at '" + classpathResource + "'");
            }
            return extractCatalog(loadDocument(inputStream));

        } catch (IOException e) {
            throw new RegistratorException("Could not load catalog from '" + classpathResource
                    + "': " + e.getMessage(), e);
        }
    }

    private Document loadDocument(InputStream inputStream) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        try {
            return factory.newDocumentBuilder().parse(inputStream);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RegistratorException("Error while parsing XML from '" + classpathResource
                    + "': " + e.getMessage(), e);
        }
    }

    private Catalog extractCatalog(Document document) {
        final Element catalog = document.getDocumentElement();
        if (catalog == null || !CATALOG_ELEMENT.equals(catalog.getNodeName())) {
            throw new RegistratorException("Invalid service catalog found at '" + classpathResource + "'");
        }
        final Catalog.Builder builder = Catalog.newCatalog();
        if (catalog.hasAttribute(CATALOG_DELAY)) {
            builder.withDelay(catalog.getAttribute(CATALOG_DELAY));
        }
        extractServices(catalog, builder);
        extractKeyValuePairs(catalog, builder);
        return builder.build();
    }

    private void extractServices(Element catalog, Catalog.Builder builder) {
        final NodeList services = catalog.getElementsByTagName(SERVICE_ELEMENT);
        final int length = services.getLength();
        for (int i = 0; i < length; i++) {
            extractService((Element) services.item(i), builder.newService());
        }
    }

    private void extractService(Element service, Service.Builder builder) {
        if (service.hasAttribute(SERVICE_NAME)) {
            builder.withName(service.getAttribute(SERVICE_NAME));
        }
        if (service.hasAttribute(SERVICE_ID)) {
            builder.withId(service.getAttribute(SERVICE_ID));
        }
        if (service.hasAttribute(SERVICE_ADDRESS)) {
            builder.withAddress(service.getAttribute(SERVICE_ADDRESS));
        }
        if (service.hasAttribute(SERVICE_PORT)) {
            builder.withPort(service.getAttribute(SERVICE_PORT));
        }
        extractHealthCheck(service, builder);
        extractTags(service, builder);
        builder.build();
    }

    private void extractHealthCheck(Element service, Service.Builder builder) {
        final NodeList checks = service.getElementsByTagName(HTTP_CHECK_ELEMENT);
        if (checks.getLength() == 0) {
            return;
        }
        final Element check = (Element) checks.item(0);
        if (check.hasAttribute(HTTP_CHECK_URL)) {
            builder.withHttpCheckUrl(check.getAttribute(HTTP_CHECK_URL));
        }
        if (check.hasAttribute(HTTP_CHECK_INTERVAL)) {
            builder.withHttpCheckInterval(check.getAttribute(HTTP_CHECK_INTERVAL));
        }
    }

    private void extractTags(Element service, Service.Builder builder) {
        final NodeList tags = service.getElementsByTagName(TAG_ELEMENT);
        final int length = tags.getLength();
        for (int i = 0; i < length; i++) {
            final Element tag = (Element) tags.item(i);
            if (tag.hasAttribute(TAG_NAME)) {
                builder.withTag(tag.getAttribute(TAG_NAME));
            }
        }
    }

    private void extractKeyValuePairs(Element catalog, Catalog.Builder builder) {
        final NodeList keyValuePairs = catalog.getElementsByTagName(KEY_ELEMENT);
        final int length = keyValuePairs.getLength();
        for (int i = 0; i < length; i++) {
            extractKeyValuePair((Element) keyValuePairs.item(i), builder);
        }
    }

    private void extractKeyValuePair(Element keyValuePair, Catalog.Builder builder) {
        if (keyValuePair.hasAttribute(KEY_NAME) && keyValuePair.hasAttribute(KEY_VALUE)) {
            builder.withKeyValuePair(
                    keyValuePair.getAttribute(KEY_NAME),
                    keyValuePair.getAttribute(KEY_VALUE)
            );
        }
    }
}
