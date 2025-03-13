UPDATE mutual_fund_config
SET download_url = 'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xls'
WHERE download_url = 'https://mf.nipponindiaim.com/InvestorServices/FactsheetsDocuments/NIMF-MONTHLY-PORTFOLIO-${DATE_MAPPER_1}.xlsx';