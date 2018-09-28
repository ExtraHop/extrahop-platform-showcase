# Azure Integration

## Description

The ExtraHop supported Azure integration delivers unparalleled visibility into real-time IT operations that help you make the most of Microsoft Azure and gain control over your cloud and hybrid IT environments. While ExtraHop delivers insights from all data in flight observed on your network, by integrating with Azure Monitor and Activity Logs, you get a centralized view of all IT assets in a way that's never been possible before.

This integration includes visibility over your entire Azure subscription for Virtual Machines, Storage Accounts, SQL Server Databases, Load Balancers, and Activity Logs. From resource utilization to performance, access changes to Azure Security Center events, we surface the information that matters.

![Azure Storage Dashboard](https://drive.google.com/file/d/1-bwkQ-qMD4TsgEtGduvyRQS6kbO-LZNf/view?usp=sharing)

![Azure Virtual Machines Dashboard](https://drive.google.com/file/d/1-b9_bNzBJ-a0D_9pfjCE6d2P3dhXGB-t/view?usp=sharing)

![Azure Databases Dashboard](https://drive.google.com/file/d/1-_NBIwXd0LyBUMtcCqjsac7X9tD7U6zD/view?usp=sharing)

## Bundle Contents

-   (5) Dashboards
    -   Azure: Activity Log
    -   Azure: Databases
    -   Azure: Load Balancers
    -   Azure: Storage
    -   Azure: Virtual Machines
-   (1) Record Formats
    -   Azure Activity Log
-   (5) Triggers
    -   Azure: Activity Log
    -   Azure: Databases
    -   Azure: Load Balancers
    -   Azure: Storage Accounts
    -   Azure: Virtual Machines

## Requirements

-   ExtraHop version 7.4 or later

## Installation Instructions

Here is an overview of the steps you must complete to integrate Azure with ExtraHop:

1.  Deploy an ExtraHop EDA and EXA in Azure

2.  Install the ExtraHop Azure Integration bundle on the EDA

3.  Enable Open Data Context on the EDA

4.  Deploy the Azure Integration

    a.  Set up the VNET Integration

    b.  Grant read permission to the App Service

    c.  Tag the Azure resources that you want to monitor

    d.  Send wire data from all tagged Azure Virtual Machines

5.  Create a Device Group for the Azure Virtual Machines on the EDA

### 1. Deploy an ExtraHop EDA and EXA in Azure

If you don't already have an EDA and EXA deployed in Azure, you must
complete the following steps:

1.  Log into Azure and complete the instructions in the following topic: [Deploy the ExtraHop Discover Appliance in Azure](https://docs.extrahop.com/7.3/deploy-eda-azure/).

2.  Log into Azure and complete the instructions in the following topic: [Deploy the ExtraHop Explore Appliance in Azure](https://docs.extrahop.com/7.3/deploy-exa-azure/).

### 2. Install the ExtraHop Azure Integration bundle on the EDA

1.  Download the bundle on this page.

2.  Log into the ExtraHop Web UI on your EDA and complete the instructions in the following topic: [Upload and Apply the Bundle](https://docs.extrahop.com/current/install-a-bundle/#upload-and-apply-a-bundle). Be sure to enable each of the Azure triggers.

### 3. Enable Open Data Context on the EDA

1.  Log into the ExtraHop Admin UI on your EDA and complete the instructions in the following topic: [Enable Open Data Context](https://docs.extrahop.com/current/import-external-data-odcapi/).

    Note: For this integration, you only need to enable default TCP port 11211.

### 4. Deploy the Azure Integration

1.  Click the Deploy to Azure button to begin the Azure Integration deployment.
    [![](http://azuredeploy.net/deploybutton.png)](https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fraw.githubusercontent.com%2FExtraHop%2Fextrahop-platform-showcase%2Fazure-integration%2Fextrahop_azure_integration_deployment.json)

2.  Sign into your Azure account.

3.  In the Basics section, complete the following steps:

    a.  In the Subscription field, select the subscription where the EDA is deployed.

    b.  In the Resource group field, select **Use existing** and then select the resource group where the EDA is deployed.

    c.  In the Location field, select a desired region.

4.  In the Settings section, fill out each of the required fields. Hover over the **( i )** on each field to see an explanation about the field.

5.  Accept the terms and conditions.

6.  Click **Purchase.**

The deployment will take approximately 30 minutes. After the deployment is complete, continue onto the next step.

#### Setup the VNET Integration

Before the deployed Function App can communicate with the EDA, you must configure a Point-to-Site VPN, which Azure refers to as a VNET integration.

1.  Navigate to Function Apps.

2.  Select the Function App Name that was specified in the deployment.

3.  Click **Platform features**.

4.  Click **Networking.**
    ![](https://drive.google.com/file/d/1-Rzbmv0lknXjypSDg6p4tuPV_y7FTEcB/view?usp=sharing)

5.  In the VNET Integration section, click **Setup**.

6.  Select the Virtual Network that was specified in the deployment.
    ![](https://drive.google.com/file/d/1-3CqIznnhW7Eyf-4-FVBqmn9OBIvUNRi/view?usp=sharing)

    If the operation fails due to a timeout (such as in the figure below), you must sync the network through the following steps before proceeding to the next section:
    ![](https://drive.google.com/file/d/1-9brl1zsludYqGYOXBz_5G2AD3hq89cM/view?usp=sharing)

    a.  In the Function App Platform features section, click **App Service plan**.
    ![](https://drive.google.com/file/d/1-52rntqcDrrrRJI7Y5akftFblhbr27uH/view?usp=sharing)

    b.  In the App Service plan Settings section, click **Networking**.

    c.  In the VNET Integration section, click **Click here to manage**.

    d.  Select the Virtual Network that was specified in the deployment.

    e.  Click **Sync Network**.
    ![](https://drive.google.com/file/d/1-JVUjCdYAltIU4_t3F1ZC7UUzms1Pb88/view?usp=sharing)

    Wait until all of the sync operations are complete and then verify that they were successful.

    If any of the sync operations were unsuccessful, wait a few minutes and click **Sync Network** again. Repeat this process until all of the sync operations complete successfully.

#### Grant read permission to the App Service

Before the deployed Function App can access metrics from all of the resources in the subscription, you must add a reader permission on the subscription level.

1.  Navigate to Subscriptions.

2.  Select the subscription specified in the deployment.

3.  Click **Access Control (IAM)**.

4.  Click **+ Add**.

5.  Configure the permission with the following configuration steps:

    a.  In the Role field, select **Reader**.

    b.  In the Assign access to field, select **Function App**.

    c.  In the subscription field, ensure that the subscription specified in the deployment is selected.

    d.  Select the Function App Name that was specified in the deployment.

![](https://drive.google.com/file/d/1-Vvs-8WF9NSBZekN7l1kxuyKIhIjaPFK/view?usp=sharing)

#### Tag the Azure resources that you want to monitor

You must go through each of the following Azure services supported by this integration and bulk assign a tag to the specific resources that you are interested in having Azure monitor metrics.

-   Virtual machines
-   Storage accounts
-   Load balancers
-   SQL databases

You must complete the following steps for each Azure Service above.

1.  Navigate to the service within Azure.

2.  Select all of the resources that you would like to monitor.

3.  Click Assign tags.

4.  In the Name field, type extrahop-azure-integration.

5.  In the Value field, type true.

6.  Click **Assign**

![](https://drive.google.com/file/d/1-TEzEKefYbS5RVOuyi_VAhh7U3PW6HwI/view?usp=sharing)

Note: If you would like to add all resources within a particular resource group, complete the following steps:

1.  Navigate to the Resource Group.

2.  Check **Select All**.

3.  Click **Assign Tags**.

4.  Enter the tag information from above.

#### Send wire data from all tagged Azure Virtual Machines

Before the Azure Integration can properly correlate virtual machine metrics with the associated devices within the EDA, you must be receiving wire data from those virtual machines.

You can either complete the steps in the [Packet Forwarding with RPCAP guide](https://docs.extrahop.com/7.3/rpcap/) or use [Azure virtual network TAP](https://docs.microsoft.com/en-us/azure/virtual-network/virtual-network-tap-overview) by completing the steps in the [Work with a virtual network TAP using the Azure CLI guide](https://docs.microsoft.com/en-us/azure/virtual-network/tutorial-tap-virtual-network-cli).

### 5. Create a Device Group for the Azure Virtual Machines on the EDA

The source for the Virtual Machines dashboard on the EDA depends on a device group that contains all of the tagged Azure Virtual Machine devices that are sending wire data.

1.  [Create a static device group](https://docs.extrahop.com/7.3/create-device-group/#create-a-static-device-group) named **Azure Virtual Machines**.

2.  Add each of the tagged Azure Virtual Machine devices that are sending wire data to the Azure Virtual Machines group.

Note: The time it takes for the EDA to discover the L3 devices for each of the Azure Virtual Machines can vary depending on how active the devices are and how much traffic is generated.