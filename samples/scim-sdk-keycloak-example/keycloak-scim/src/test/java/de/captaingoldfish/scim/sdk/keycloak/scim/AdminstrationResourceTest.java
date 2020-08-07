package de.captaingoldfish.scim.sdk.keycloak.scim;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.captaingoldfish.scim.sdk.common.constants.AttributeNames;
import de.captaingoldfish.scim.sdk.common.constants.HttpStatus;
import de.captaingoldfish.scim.sdk.common.constants.ResourceTypeNames;
import de.captaingoldfish.scim.sdk.common.resources.ServiceProvider;
import de.captaingoldfish.scim.sdk.common.resources.complex.BulkConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.ChangePasswordConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.ETagConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.FilterConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.Meta;
import de.captaingoldfish.scim.sdk.common.resources.complex.PatchConfig;
import de.captaingoldfish.scim.sdk.common.resources.complex.SortConfig;
import de.captaingoldfish.scim.sdk.common.utils.JsonHelper;
import de.captaingoldfish.scim.sdk.keycloak.entities.ScimResourceTypeEntity;
import de.captaingoldfish.scim.sdk.keycloak.entities.ScimServiceProviderEntity;
import de.captaingoldfish.scim.sdk.keycloak.scim.resources.ParseableResourceType;
import de.captaingoldfish.scim.sdk.keycloak.services.ScimResourceTypeService;
import de.captaingoldfish.scim.sdk.keycloak.services.ScimServiceProviderServiceBridge;
import de.captaingoldfish.scim.sdk.keycloak.setup.KeycloakScimManagementTest;
import de.captaingoldfish.scim.sdk.server.schemas.ResourceType;
import de.captaingoldfish.scim.sdk.server.schemas.custom.ETagFeature;
import de.captaingoldfish.scim.sdk.server.schemas.custom.EndpointControlFeature;
import de.captaingoldfish.scim.sdk.server.schemas.custom.ResourceTypeAuthorization;
import de.captaingoldfish.scim.sdk.server.schemas.custom.ResourceTypeFeatures;


/**
 * @author Pascal Knueppel
 * @since 07.08.2020
 */
public class AdminstrationResourceTest extends KeycloakScimManagementTest
{

  /**
   * the endpoint under test
   */
  private AdminstrationResource administrationResource;


  /**
   * initializes the endpoint
   */
  @BeforeEach
  public void initTests()
  {
    administrationResource = new AdminstrationResource(getKeycloakSession(), getAuthentication());
  }

  /**
   * verifies that the configuration can successfully be updated
   */
  @Test
  public void testUpdateServiceProviderConfiguration()
  {
    ServiceProvider serviceProvider = ServiceProvider.builder()
                                                     .filterConfig(FilterConfig.builder()
                                                                               .supported(false)
                                                                               .maxResults(5)
                                                                               .build())
                                                     .sortConfig(SortConfig.builder().supported(false).build())
                                                     .patchConfig(PatchConfig.builder().supported(false).build())
                                                     .eTagConfig(ETagConfig.builder().supported(false).build())
                                                     .changePasswordConfig(ChangePasswordConfig.builder()
                                                                                               .supported(true)
                                                                                               .build())
                                                     .bulkConfig(BulkConfig.builder()
                                                                           .supported(false)
                                                                           .maxOperations(1)
                                                                           .maxPayloadSize(1L)
                                                                           .build())
                                                     .build();

    Response response = administrationResource.updateServiceProviderConfig(serviceProvider.toString());
    Assertions.assertEquals(HttpStatus.OK, response.getStatus());

    verifyDatabaseSetupIsNotEqual(ScimServiceProviderServiceBridge.getDefaultServiceProvider(getKeycloakSession()));
    ServiceProvider updatedProvider = JsonHelper.readJsonDocument((String)response.getEntity(), ServiceProvider.class);
    verifyServiceProviderMatchesDatabaseEntry(updatedProvider);
    verifyServiceProviderMatchesDatabaseEntry(administrationResource.getResourceEndpoint().getServiceProvider());
  }

  /**
   * verifies that the initial database data of the service provider matches the expected initial setup
   */
  private void verifyDatabaseSetupIsNotEqual(ServiceProvider sp)
  {
    ScimServiceProviderEntity serviceProvider = getEntityManager().createNamedQuery("getScimServiceProvider",
                                                                                    ScimServiceProviderEntity.class)
                                                                  .setParameter("realmId", getRealmModel().getId())
                                                                  .getSingleResult();
    Assertions.assertEquals(getRealmModel().getId(), serviceProvider.getRealmId());
    Assertions.assertNotEquals(sp.getFilterConfig().isSupported(), serviceProvider.isFilterSupported());
    Assertions.assertNotEquals(sp.getFilterConfig().getMaxResults(), serviceProvider.getFilterMaxResults());
    Assertions.assertNotEquals(sp.getSortConfig().isSupported(), serviceProvider.isSortSupported());
    Assertions.assertNotEquals(sp.getPatchConfig().isSupported(), serviceProvider.isPatchSupported());
    Assertions.assertNotEquals(sp.getETagConfig().isSupported(), serviceProvider.isEtagSupported());
    Assertions.assertNotEquals(sp.getChangePasswordConfig().isSupported(), serviceProvider.isChangePasswordSupported());
    Assertions.assertNotEquals(sp.getBulkConfig().isSupported(), serviceProvider.isBulkSupported());
    Assertions.assertNotEquals(sp.getBulkConfig().getMaxOperations(), serviceProvider.getBulkMaxOperations());
    Assertions.assertNotEquals(sp.getBulkConfig().getMaxPayloadSize(), serviceProvider.getBulkMaxPayloadSize());
    Assertions.assertNotEquals(sp.getMeta().flatMap(Meta::getCreated).orElseThrow(IllegalStateException::new),
                               serviceProvider.getCreated());
    Assertions.assertNotEquals(sp.getMeta().flatMap(Meta::getLastModified).orElseThrow(IllegalStateException::new),
                               serviceProvider.getLastModified());
  }

  /**
   * verifies that the initial database data of the service provider matches the expected initial setup
   */
  private void verifyServiceProviderMatchesDatabaseEntry(ServiceProvider sp)
  {
    ScimServiceProviderEntity serviceProvider = getEntityManager().createNamedQuery("getScimServiceProvider",
                                                                                    ScimServiceProviderEntity.class)
                                                                  .setParameter("realmId", getRealmModel().getId())
                                                                  .getSingleResult();
    Assertions.assertEquals(getRealmModel().getId(), serviceProvider.getRealmId());
    Assertions.assertEquals(sp.getFilterConfig().isSupported(), serviceProvider.isFilterSupported());
    Assertions.assertEquals(sp.getFilterConfig().getMaxResults(), serviceProvider.getFilterMaxResults());
    Assertions.assertEquals(sp.getSortConfig().isSupported(), serviceProvider.isSortSupported());
    Assertions.assertEquals(sp.getPatchConfig().isSupported(), serviceProvider.isPatchSupported());
    Assertions.assertEquals(sp.getETagConfig().isSupported(), serviceProvider.isEtagSupported());
    Assertions.assertEquals(sp.getChangePasswordConfig().isSupported(), serviceProvider.isChangePasswordSupported());
    Assertions.assertEquals(sp.getBulkConfig().isSupported(), serviceProvider.isBulkSupported());
    Assertions.assertEquals(sp.getBulkConfig().getMaxOperations(), serviceProvider.getBulkMaxOperations());
    Assertions.assertEquals(sp.getBulkConfig().getMaxPayloadSize(), serviceProvider.getBulkMaxPayloadSize());
    Assertions.assertEquals(sp.getMeta().flatMap(Meta::getCreated).orElseThrow(IllegalStateException::new),
                            serviceProvider.getCreated());
    Assertions.assertEquals(sp.getMeta().flatMap(Meta::getLastModified).orElseThrow(IllegalStateException::new),
                            serviceProvider.getLastModified());
  }

  /**
   * verifies that a resource type configuration can be successfully updated
   */
  @ParameterizedTest
  @ValueSource(strings = {ResourceTypeNames.USER, ResourceTypeNames.GROUPS})
  public void testUpdateResourceTypeConfig(String resourceTypeName)
  {
    final String description = "a useless description";
    final boolean autoFiltering = false;
    final boolean autoSorting = false;
    final boolean autoEtags = true;
    final boolean disableEndpoint = true;
    final boolean requireAuthentication = true;

    ParseableResourceType parseableResourceType = new ParseableResourceType();
    parseableResourceType.setName(resourceTypeName);
    parseableResourceType.setDescription(description);

    parseableResourceType.getFeatures().setAutoFiltering(autoFiltering);
    parseableResourceType.getFeatures().setAutoSorting(autoSorting);
    parseableResourceType.getFeatures().setETagFeature(ETagFeature.builder().enabled(autoEtags).build());
    parseableResourceType.getFeatures().setResourceTypeDisabled(disableEndpoint);

    parseableResourceType.getFeatures().getEndpointControlFeature().setCreateDisabled(disableEndpoint);
    parseableResourceType.getFeatures().getEndpointControlFeature().setGetDisabled(disableEndpoint);
    parseableResourceType.getFeatures().getEndpointControlFeature().setListDisabled(disableEndpoint);
    parseableResourceType.getFeatures().getEndpointControlFeature().setUpdateDisabled(disableEndpoint);
    parseableResourceType.getFeatures().getEndpointControlFeature().setDeleteDisabled(disableEndpoint);

    parseableResourceType.getFeatures().getAuthorization().setAuthenticated(requireAuthentication);

    Response response = administrationResource.updateResourceType(resourceTypeName, parseableResourceType.toString());
    ParseableResourceType updatedResourceType = JsonHelper.readJsonDocument((String)response.getEntity(),
                                                                            ParseableResourceType.class);
    // removes the non updatable attributes from the response to prepare a comparison of the two given objects
    {
      updatedResourceType.remove(AttributeNames.RFC7643.SCHEMAS);
      updatedResourceType.remove(AttributeNames.RFC7643.ID);
      updatedResourceType.remove(AttributeNames.RFC7643.ENDPOINT);
      updatedResourceType.remove(AttributeNames.RFC7643.SCHEMA);
      updatedResourceType.remove(AttributeNames.RFC7643.SCHEMA_EXTENSIONS);
      updatedResourceType.remove(AttributeNames.RFC7643.META);
    }
    Assertions.assertEquals(parseableResourceType, updatedResourceType);
    compareDatabaseEntryWithReturnedData((String)response.getEntity());
    compareActualConfigWithReturnedData((String)response.getEntity());
  }

  /**
   * verifies that the given response content matches the data within the database
   */
  private void compareDatabaseEntryWithReturnedData(String responseContent)
  {
    ParseableResourceType updatedResourceType = JsonHelper.readJsonDocument(responseContent,
                                                                            ParseableResourceType.class);
    ScimResourceTypeService resourceTypeService = new ScimResourceTypeService(getKeycloakSession());

    final String resourceTypeName = updatedResourceType.getName();
    ScimResourceTypeEntity resourceTypeEntity = resourceTypeService.getResourceTypeEntityByName(resourceTypeName).get();
    Assertions.assertNotNull(resourceTypeEntity.getLastModified());
    Assertions.assertNotEquals(resourceTypeEntity.getCreated(), resourceTypeEntity.getLastModified());

    Assertions.assertEquals(updatedResourceType.getDescription().get(), resourceTypeEntity.getDescription());

    ResourceTypeFeatures features = updatedResourceType.getFeatures();
    Assertions.assertEquals(features.isResourceTypeDisabled(), !resourceTypeEntity.isEnabled());
    Assertions.assertEquals(features.isAutoFiltering(), resourceTypeEntity.isAutoFiltering());
    Assertions.assertEquals(features.isAutoSorting(), resourceTypeEntity.isAutoSorting());

    EndpointControlFeature endpointControl = features.getEndpointControlFeature();
    Assertions.assertEquals(endpointControl.isCreateDisabled(), resourceTypeEntity.isDisableCreate());
    Assertions.assertEquals(endpointControl.isGetDisabled(), resourceTypeEntity.isDisableGet());
    Assertions.assertEquals(endpointControl.isListDisabled(), resourceTypeEntity.isDisableList());
    Assertions.assertEquals(endpointControl.isUpdateDisabled(), resourceTypeEntity.isDisableUpdate());
    Assertions.assertEquals(endpointControl.isDeleteDisabled(), resourceTypeEntity.isDisableDelete());

    ResourceTypeAuthorization authorization = features.getAuthorization();
    Assertions.assertEquals(authorization.isAuthenticated(), resourceTypeEntity.isRequireAuthentication());
  }

  /**
   * verifies that the given response content matches the actual active configuration
   */
  private void compareActualConfigWithReturnedData(String responseContent)
  {
    ParseableResourceType updatedResourceType = JsonHelper.readJsonDocument(responseContent,
                                                                            ParseableResourceType.class);
    final String resourceTypeName = updatedResourceType.getName();
    ResourceType resourceType = administrationResource.getResourceEndpoint()
                                                      .getResourceTypeByName(resourceTypeName)
                                                      .get();

    Assertions.assertEquals(updatedResourceType.getDescription().get(), resourceType.getDescription().get());

    ResourceTypeFeatures features = updatedResourceType.getFeatures();
    ResourceTypeFeatures actualFeatures = resourceType.getFeatures();
    Assertions.assertEquals(features.isResourceTypeDisabled(), actualFeatures.isResourceTypeDisabled());
    Assertions.assertEquals(features.isAutoFiltering(), actualFeatures.isAutoFiltering());
    Assertions.assertEquals(features.isAutoSorting(), actualFeatures.isAutoSorting());

    EndpointControlFeature endpointControl = features.getEndpointControlFeature();
    EndpointControlFeature actualEndpointControl = actualFeatures.getEndpointControlFeature();
    Assertions.assertEquals(endpointControl.isCreateDisabled(), actualEndpointControl.isCreateDisabled());
    Assertions.assertEquals(endpointControl.isGetDisabled(), actualEndpointControl.isGetDisabled());
    Assertions.assertEquals(endpointControl.isListDisabled(), actualEndpointControl.isListDisabled());
    Assertions.assertEquals(endpointControl.isUpdateDisabled(), actualEndpointControl.isUpdateDisabled());
    Assertions.assertEquals(endpointControl.isDeleteDisabled(), actualEndpointControl.isDeleteDisabled());

    ResourceTypeAuthorization authorization = features.getAuthorization();
    ResourceTypeAuthorization actualAuthorization = actualFeatures.getAuthorization();
    Assertions.assertEquals(authorization.isAuthenticated(), actualAuthorization.isAuthenticated());
  }
}
