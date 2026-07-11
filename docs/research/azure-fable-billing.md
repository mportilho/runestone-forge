# Azure/Microsoft Foundry: Claude Fable 5 billing and contractual notes

Research date: 2026-07-11

Scope: public primary/official sources from Microsoft Azure/Microsoft Foundry/Microsoft Marketplace and Anthropic. This note focuses on whether `claude-fable-5` / Claude Fable 5 in Microsoft Foundry has contractual limitations or extra billing beyond normal usage/token charges.

## Short Answer

Public documentation does mention `claude-fable-5` specifically. It is listed as an Anthropic Claude model in Microsoft Foundry, currently `Hosted on Anthropic: Preview`, not as an Azure OpenAI model sold directly by Azure. The public evidence points to the normal Claude-on-Foundry commercial path: Azure Marketplace subscription, Anthropic publisher terms, pay-as-you-go usage converted to Claude Consumption Units (CCU), and charges billed by Microsoft on the Azure invoice. I found no public evidence of a Fable-specific contractual surcharge or separate seat/license charge beyond Anthropic's per-model usage pricing, CCU conversion, and any optional feature/tool charges that apply if those features are used.

## Key Findings

- Microsoft lists `claude-fable-5` in the official Claude models page for Microsoft Foundry as `Hosted on Anthropic: Preview`, with 1M context and 128K max output. The same page says Claude models are accessed through "Foundry Models from partners and community", are Non-Microsoft Products under the Product Terms when Anthropic sells/operates them, require an Azure Marketplace subscription, and are billed through the Claude Consumption Units (CCU) article. Source: [Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models).

- Microsoft's "Foundry Models from partners and community" page includes `claude-fable-5` in the Anthropic section and states that partner/community models not sold by Azure are Non-Microsoft Products under the Product Terms. It also states that model providers define license terms and set the price through Azure Marketplace. Source: [Foundry Models from partners and community](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-from-partners).

- Deploying partner/community models requires Azure Marketplace subscription/acceptance. Microsoft deployment docs say partner/community models require subscribing to Azure Marketplace and accepting terms with **Agree and Proceed**; models sold by Azure, such as Azure OpenAI models, do not have that Marketplace subscription requirement. Source: [Deploy Microsoft Foundry Models in the Foundry portal](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/how-to/deploy-foundry-models).

- Claude models in Foundry require Marketplace-related permissions, including agreement read/sign actions and `Microsoft.SaaS/register/action`; Owner and Contributor roles include the listed permissions. Source: [Foundry Models from partners and community, permissions section](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-from-partners#permissions-required-to-subscribe-to-models-from-partners-and-community).

- Microsoft states that Claude models in Foundry use CCU as the billing unit for all Claude models offered in Foundry. Token usage is converted to CCU using Anthropic's published per-model token rates after discounts. CCU is the single billing dimension, metered hourly to Azure Marketplace, and rolls up onto the Azure invoice. Source: [Claude consumption units (CCU) billing in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-billing).

- Microsoft states CCU billing is pay-as-you-go through Azure Marketplace, has no prepaid CCU credits, is invoiced monthly in arrears, appears as CCU in Azure Cost Management, and preserves the Microsoft billing relationship: invoice, payment terms, and tax handling remain with Microsoft. Source: [Claude consumption units (CCU) billing in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-billing).

- Microsoft states CCU is a billing format change, not a price change: cost is driven by Anthropic's per-model token rates and any private-offer discounts. This is the strongest public evidence against a separate Fable-specific surcharge in the Microsoft billing path. Source: [Claude consumption units (CCU) billing in Microsoft Foundry, FAQ](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-billing#frequently-asked-questions).

- Anthropic's official pricing page lists Claude Fable 5 pricing as $10 per million base input tokens, $12.50 per million 5-minute cache writes, $20 per million 1-hour cache writes, $1 per million cache hits/refreshes, and $50 per million output tokens. It also states that Claude in Microsoft Foundry bills through Azure Marketplace using CCUs at $0.01 per CCU, with token usage rated in USD, discounts applied, converted to CCU, reported hourly to Azure Marketplace, and shown as a single CCU line item on the Azure bill. Source: [Anthropic pricing](https://docs.claude.com/en/docs/about-claude/pricing).

- Anthropic's official Foundry guide says Claude in Microsoft Foundry is billed for Claude usage in Azure Marketplace, managed through the Azure subscription, denominated in CCUs, metered hourly, and invoiced monthly in arrears on the Azure bill. Source: [Claude in Microsoft Foundry](https://docs.claude.com/en/docs/build-with-claude/claude-in-microsoft-foundry).

- Microsoft states both Claude hosting options, `Hosted on Azure` and `Hosted on Anthropic`, use the same purchasing flow: subscribe to the Claude Platform on Foundry offer through Azure Marketplace or the Foundry portal model catalog, accept the offer on the Azure billing account, deploy the model, and usage is metered/billed in CCU. Source: [Compare hosting options for Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-hosting-comparison).

- For both Claude hosting options, Microsoft says Anthropic is seller and operator, Claude models are Non-Microsoft Products under Product Terms, and use is subject to Anthropic's terms of use for Claude models and APIs. Source: [Compare hosting options for Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-hosting-comparison) and [Data, privacy, and security for Claude models](https://learn.microsoft.com/en-us/azure/foundry/responsible-ai/claude-models/data-privacy).

- Microsoft Marketplace Terms say Publisher Offers are governed by separate Publisher Terms; Microsoft is not a party to Publisher Terms, acts as the authorized representative of the publisher for sale, and processes payments. The purchase contract for a transactable Publisher Offer is concluded directly between customer and publisher. Source: [Microsoft Marketplace Terms of Use](https://learn.microsoft.com/en-us/legal/marketplace/marketplace-terms).

- Marketplace SaaS purchase docs say the checkout flow requires reviewing publisher terms, amendments, privacy policy, and Microsoft Marketplace terms before subscribing. They also describe metered/pay-as-you-go SaaS plans where the publisher sets the unit of measure and price per unit. Source: [How to purchase a SaaS offer in the Azure portal](https://learn.microsoft.com/en-us/marketplace/purchase-saas-offer-in-azure-portal).

- For EA/MCA/MOSP/MPA billing, Microsoft Marketplace docs state Marketplace purchases are included in Microsoft invoices, with different presentation depending on billing account type. EA customers generally receive a consolidated invoice including Azure and Marketplace charges, with some country exceptions; MCA invoices include Azure usage and Marketplace purchases; MOSP can receive separate Marketplace invoices. Source: [Overview of billing and invoicing for Microsoft Marketplace customers](https://learn.microsoft.com/en-us/marketplace/billing-invoicing).

- For MACC, Microsoft says eligible Marketplace purchases can decrement the Microsoft Azure Consumption Commitment. Microsoft specifically says CCU spend decrements MACC, and the Marketplace MACC page says eligible Marketplace offers can count 100% of pretax purchase amount toward the commitment when purchased correctly through Marketplace/Azure portal. Sources: [Claude CCU billing](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-billing) and [Azure Consumption Commitment Benefit](https://learn.microsoft.com/en-us/marketplace/azure-consumption-commitment-benefit).

- For Azure Prepayment, Microsoft Foundry cost docs state Azure Prepayment can pay for Models sold by Azure, but cannot pay for charges for other provider models because they are billed through Azure Marketplace. Microsoft Marketplace Terms also say subscription credits and Azure prepayment funds cannot be used to purchase Marketplace offers unless indicated otherwise for the offer. Sources: [Plan and manage costs for Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/concepts/manage-costs) and [Microsoft Marketplace Terms of Use](https://learn.microsoft.com/en-us/legal/marketplace/marketplace-terms).

- For CSP and subscription restrictions, Microsoft's Anthropic section states Claude requires a paid Azure subscription with a billing account in a country/region where Anthropic offers models for purchase. Unsupported subscription types include Enterprise Accounts in South Korea, Cloud Solution Provider subscriptions, subscriptions without active pay-as-you-go billing, and sponsored subscriptions that only use Azure credits. Source: [Foundry Models from partners and community, Anthropic subscription type and region support](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-from-partners#anthropic).

- For Enterprise/MCA-E differences, the public difference found is not a different contract fee but quota/rate limit. Microsoft's Claude page shows `claude-fable-5` default rate limits as 0 RPM/0 ITPM for Pay-as-you-go and Free Trial, but 2,000 RPM/2,000,000 ITPM for Enterprise and MCA-E. Source: [Claude models in Microsoft Foundry, rate limits by subscription type](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models#rate-limits-by-subscription-type).

- Fable-specific operational limitations were found, but not Fable-specific billing surcharges. Microsoft says Claude Fable 5 applies additional input/output classifiers that may refuse requests triggering dual-use safeguard policies; a refusal returns HTTP 200 with `stop_reason: "refusal"`, and input tokens that are refused are not billed. Microsoft also lists Fable parameter constraints, including `top_p` must be at least 0.99 and `top_k`, `temperature`, `thinking={"type":"enabled"}`, `thinking={"type":"disabled"}`, and `output_format` are unsupported. Sources: [Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models) and [Foundry Models from partners and community](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-from-partners#anthropic).

- Optional Anthropic features can add charges beyond base input/output tokens, but these are feature/tool charges, not Fable-specific contractual charges. Anthropic pricing states server-side tools may incur additional usage charges, web search is $10 per 1,000 searches plus token costs, code execution can be billed by execution time when used without web search/fetch after free hours, web fetch has no additional charge beyond token costs, and tool definitions/system prompts add input tokens. Source: [Anthropic pricing, feature-specific pricing](https://docs.claude.com/en/docs/about-claude/pricing#feature-specific-pricing).

- Public Microsoft and Anthropic docs confirm `claude-fable-5` availability in Foundry. I did not find public evidence that Fable requires a separate Anthropic seat license, a separate non-Azure invoice, or a Fable-specific platform fee beyond CCU/token/feature metering and any negotiated private offer terms. Sources: [Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models), [Claude in Microsoft Foundry](https://docs.claude.com/en/docs/build-with-claude/claude-in-microsoft-foundry), and [Anthropic pricing](https://docs.claude.com/en/docs/about-claude/pricing).

## Direct Answers to the Requested Questions

### 1. Do partner models such as Anthropic in Foundry require specific terms/subscription acceptance?

Yes. Claude is treated as a partner/community model and requires Azure Marketplace subscription/acceptance before deployment. Use is also subject to Anthropic-provided terms for Claude models/APIs. The relevant acceptance surfaces are the Azure Marketplace offer or the Microsoft Foundry deployment flow.

### 2. Does billing appear as Azure consumption, Azure Marketplace, passthrough, or separate billing?

The public docs describe it as Azure Marketplace metering and Microsoft billing on the Azure invoice. Claude usage is converted to CCU and billed through Azure Marketplace; Azure Cost Management shows aggregated CCU, while Foundry can show per-model token/request details. Microsoft Marketplace invoicing can appear consolidated or separate depending on billing account type and geography.

### 3. Is there a difference for EA/MCA/CSP/Azure AI contracts?

There are differences in purchase eligibility, invoicing presentation, prepayment treatment, and quotas. EA/MCA are supported billing account types for Marketplace invoicing generally, but Microsoft and Anthropic docs list CSP subscriptions as unsupported for Claude. Enterprise and MCA-E receive higher default Fable rate limits than pay-as-you-go/free trial. Azure Prepayment cannot be used for non-Azure provider models billed through Marketplace, while CCU spend is stated to be MACC-eligible.

### 4. Is there specific mention of Fable or Claude Fable?

Yes. Microsoft lists `claude-fable-5` in Foundry documentation. Anthropic lists Claude Fable 5 in official model pricing and in the Foundry model availability table. Public sources specifically say Fable is hosted on Anthropic in Foundry, is Preview, and has Fable-specific safeguards/parameter constraints.

### 5. Is there public evidence of additional Fable-specific billing beyond normal usage/tokens?

No public evidence found for a Fable-specific contractual surcharge, separate license, or separate invoice. The public model is per-token pricing converted to CCU through Azure Marketplace. Optional Claude features/tools may add charges or extra billable tokens if used, but those are general feature-specific charges, not public Fable-only charges.

## Gaps and Items to Verify Privately

- Verify in the Azure portal whether the exact Marketplace offer shown to your tenant is **Claude Platform on Foundry**, which plan is selected, the CCU unit price shown, and whether the offer details include additional private terms or amendments.

- Verify whether your tenant has a private offer with Anthropic or Microsoft/Anthropic negotiated pricing. Public docs say private-offer discounts are applied during token-to-CCU conversion, not as separate invoice line items, but private terms can change economics.

- Verify your billing account type and country/region in Cost Management/Billing because EA, MCA, MOSP, MPA, indirect LSP, and geography can change invoice presentation and payment handling.

- Verify whether your subscription is eligible to purchase the Anthropic Marketplace offer. Public docs say CSP, Enterprise Accounts in South Korea, student/free/startup-credit-only, and Azure-credit-only sponsored subscriptions are unsupported for Claude.

- Verify whether your organization blocks Azure Marketplace purchases through EA Marketplace settings, Private Marketplace, Azure Policy, or tenant procurement controls.

- Verify whether any Azure Prepayment, MACC, or consumption commitment clauses in your private agreement override, exclude, or condition Marketplace/Anthropic purchases. Public docs say CCU is MACC-eligible, but Microsoft also notes exclusions can apply to some commitment agreements.

- Verify which deployment/version is available in your region. Public docs show Fable as Hosted on Anthropic/Global Standard only, with region availability in specific regions such as East US 2 and Sweden Central at the time of research.

- Verify whether your actual workload uses server-side tools, prompt caching, batch, data residency, managed agents, or other features that can affect cost. Anthropic pricing includes feature-specific charges and multipliers, but Foundry feature availability differs by hosting option and can change.

- Verify invoice/reconciliation exports after a small test deployment. Public docs say Cost Management and invoices are the source of truth for reconciliation, and Marketplace model meters can appear under resource-group or Global resources rather than the Foundry resource.

## Sources Consulted

- Microsoft Learn: [Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models)
- Microsoft Learn: [Claude consumption units (CCU) billing in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-billing)
- Microsoft Learn: [Compare hosting options for Claude models in Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/claude-models-hosting-comparison)
- Microsoft Learn: [Foundry Models from partners and community](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-from-partners)
- Microsoft Learn: [Foundry Models sold by Azure](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/concepts/models-sold-directly-by-azure)
- Microsoft Learn: [Deploy Microsoft Foundry Models in the Foundry portal](https://learn.microsoft.com/en-us/azure/foundry/foundry-models/how-to/deploy-foundry-models)
- Microsoft Learn: [Plan and manage costs for Microsoft Foundry](https://learn.microsoft.com/en-us/azure/foundry/concepts/manage-costs)
- Microsoft Learn: [Data, privacy, and security for Claude models](https://learn.microsoft.com/en-us/azure/foundry/responsible-ai/claude-models/data-privacy)
- Microsoft Learn Legal: [Microsoft Marketplace Terms of Use](https://learn.microsoft.com/en-us/legal/marketplace/marketplace-terms)
- Microsoft Learn Marketplace: [How to purchase a SaaS offer in the Azure portal](https://learn.microsoft.com/en-us/marketplace/purchase-saas-offer-in-azure-portal)
- Microsoft Learn Marketplace: [Overview of billing and invoicing for Microsoft Marketplace customers](https://learn.microsoft.com/en-us/marketplace/billing-invoicing)
- Microsoft Learn Marketplace: [Azure Consumption Commitment Benefit](https://learn.microsoft.com/en-us/marketplace/azure-consumption-commitment-benefit)
- Azure pricing: [Microsoft Foundry pricing](https://azure.microsoft.com/en-us/pricing/details/microsoft-foundry/)
- Anthropic docs: [Claude in Microsoft Foundry](https://docs.claude.com/en/docs/build-with-claude/claude-in-microsoft-foundry)
- Anthropic docs: [Pricing](https://docs.claude.com/en/docs/about-claude/pricing)
- Anthropic legal: [Commercial Terms of Service](https://www.anthropic.com/legal/commercial-terms)
- Anthropic legal: [Data Processing Addendum](https://www.anthropic.com/legal/data-processing-addendum)
