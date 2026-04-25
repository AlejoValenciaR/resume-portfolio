$ErrorActionPreference = 'Stop'

function Read-JsonFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
}

function Convert-MonthLabel {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Month
    )

    switch ($Month.ToLowerInvariant()) {
        'jan' { return 'Jan.' }
        'feb' { return 'Feb.' }
        'mar' { return 'Mar.' }
        'apr' { return 'Apr.' }
        'may' { return 'May' }
        'jun' { return 'Jun.' }
        'jul' { return 'Jul.' }
        'aug' { return 'Aug.' }
        'sep' { return 'Sep.' }
        'oct' { return 'Oct.' }
        'nov' { return 'Nov.' }
        'dec' { return 'Dec.' }
        default { return $Month }
    }
}

function Convert-PeriodLabel {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Period
    )

    $enDash = [char]0x2013

    if ($Period -match '^(?<startMonth>[A-Za-z]+) (?<startYear>\d{4}) - (?<endMonth>[A-Za-z]+) (?<endYear>\d{4})$') {
        return ('{0} {1} {2} {3} {4}' -f
            (Convert-MonthLabel $Matches.startMonth),
            $Matches.startYear,
            $enDash,
            (Convert-MonthLabel $Matches.endMonth),
            $Matches.endYear
        )
    }

    if ($Period -match '^(?<startYear>\d{4}) - (?<endYear>\d{4})$') {
        return ('{0} {1} {2}' -f $Matches.startYear, $enDash, $Matches.endYear)
    }

    return ($Period -replace ' - ', " $enDash ")
}

function Normalize-BulletText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Text
    )

    $trimmed = $Text.Trim()
    while ($trimmed.EndsWith('.')) {
        $trimmed = $trimmed.Substring(0, $trimmed.Length - 1).TrimEnd()
    }
    return $trimmed
}

function Get-ParagraphRangeWithoutMark {
    param(
        [Parameter(Mandatory = $true)]
        $Paragraph
    )

    $range = $Paragraph.Range.Duplicate
    if ($range.End -gt $range.Start) {
        $range.End = $range.End - 1
    }
    return $range
}

function Set-ParagraphText {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [int]$Index,
        [Parameter(Mandatory = $true)]
        [AllowEmptyString()]
        [string]$Text
    )

    $range = Get-ParagraphRangeWithoutMark $Document.Paragraphs.Item($Index)
    $range.Text = $Text
}

function Set-FirstParagraphName {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [string]$Text
    )

    # The first paragraph in the template carries an extra control character before
    # the paragraph mark, so replace the full paragraph content and then restore the
    # intended bold name line formatting.
    $paragraph = $Document.Paragraphs.Item(1)
    $paragraph.Range.Text = "$Text`r"
    $range = Get-ParagraphRangeWithoutMark $paragraph
    $range.Font.Bold = 1
}

function Set-ParagraphTextWithTab {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [int]$Index,
        [Parameter(Mandatory = $true)]
        [string]$LeftText,
        [Parameter(Mandatory = $true)]
        [string]$RightText
    )

    $range = Get-ParagraphRangeWithoutMark $Document.Paragraphs.Item($Index)
    $range.Text = "$LeftText`t$RightText"
}

function Set-BoldCompanyLine {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [int]$Index,
        [Parameter(Mandatory = $true)]
        [string]$Text
    )

    Set-ParagraphText -Document $Document -Index $Index -Text $Text
    $range = Get-ParagraphRangeWithoutMark $Document.Paragraphs.Item($Index)
    $range.Font.Bold = 1
}

function Set-EducationLine {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [int]$Index,
        [Parameter(Mandatory = $true)]
        [string]$InstitutionLine,
        [Parameter(Mandatory = $true)]
        [string]$Dates
    )

    $paragraph = $Document.Paragraphs.Item($Index)
    $range = Get-ParagraphRangeWithoutMark $paragraph
    $range.Text = "$InstitutionLine`t$Dates"
    $range.Font.Bold = 0

    $boldRange = $paragraph.Range.Duplicate
    $boldRange.End = $boldRange.Start + $InstitutionLine.Length
    $boldRange.Font.Bold = 1
}

function Clear-LanguageParagraph {
    param(
        [Parameter(Mandatory = $true)]
        $Document,
        [Parameter(Mandatory = $true)]
        [int]$Index
    )

    $paragraph = $Document.Paragraphs.Item($Index)
    [void]$paragraph.Range.ListFormat.RemoveNumbers()
    Set-ParagraphText -Document $Document -Index $Index -Text ''
}

function Copy-FormattedParagraphRange {
    param(
        [Parameter(Mandatory = $true)]
        $SourceDocument,
        [Parameter(Mandatory = $true)]
        [int]$StartIndex,
        [Parameter(Mandatory = $true)]
        [int]$EndIndex,
        [Parameter(Mandatory = $true)]
        $TargetDocument
    )

    $sourceRange = $SourceDocument.Range(
        $SourceDocument.Paragraphs.Item($StartIndex).Range.Start,
        $SourceDocument.Paragraphs.Item($EndIndex).Range.End
    )

    $targetRange = $TargetDocument.Range($TargetDocument.Content.End - 1, $TargetDocument.Content.End - 1)
    $targetRange.FormattedText = $sourceRange.FormattedText
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$templatePath = Join-Path $repoRoot 'docs\CV Globant.docx'
$outputPath = Join-Path $repoRoot 'docs\CV Globant - Alejandro Valencia Rivera.docx'
$cvRoot = Join-Path $repoRoot 'src\main\resources\cv'

$personalInfo = Read-JsonFile (Join-Path $cvRoot 'personal-info.json')
$experienceData = Read-JsonFile (Join-Path $cvRoot 'experience.json')
$educationData = Read-JsonFile (Join-Path $cvRoot 'education.json')
$languagesData = Read-JsonFile (Join-Path $cvRoot 'languages.json')

$summary = 'Alejandro Valencia Rivera is a senior backend-first full stack developer with 7+ years of experience in banking, fintech, CRM, cloud, and data platforms. He specializes in Java / Spring Boot, Python, AWS, APIs, ETL, and modernization delivery in senior developer and technical lead roles, with 2 years of Angular experience building internal operational frontends on cloud-native platforms.'

$qualifications = @(
    'Java / Spring Boot backend services',
    'Python for APIs, ETL, and automation',
    'Angular / TypeScript for internal operational frontends',
    'REST, SOAP, and SFTP integrations',
    'Microservices and Domain-Driven Design',
    'AWS architecture and Terraform delivery',
    'Oracle, PL/SQL, PostgreSQL, MongoDB, DocumentDB, and DynamoDB',
    'ETL / ELT, AWS Glue, DataStage, and Power BI',
    'Jenkins, Docker, Kubernetes, and CI/CD',
    'Core banking, CRM, and Scrum leadership'
)

$languageLines = @(
    'Spanish (native - C2)',
    'English (advanced - B2/C1)'
)

$experienceMap = @{
    'CINTE GROUP S.A. (Experian Outsourcing)' = @{
        Company = 'CINTE GROUP S.A. (Experian Outsourcing)'
        Location = 'Cali, Colombia'
        Role = 'Technical Lead, Backend & Data Engineering'
        Period = 'Jun 2024 - Dec 2025'
        Bullets = @(
            'Led Salesforce Marketing Cloud integration through API Gateway, secure SFTP exchange, and governed customer-data flows on AWS, improving campaign success by 45% and profitability by 25%',
            'Delivered an Angular and TypeScript internal operations console backed by AWS services to expose Amazon EKS pod inventories, deployment status, and cloud-runtime visibility for engineering and support teams',
            'Produced technical designs, diagrams, and delivery artifacts for a six-month integration program with cross-team dependencies and enterprise governance constraints',
            'Led the migration from on-premises MongoDB to Amazon DocumentDB, adapting roughly 400 queries across 130 components and standardizing translation utilities across Node.js, Spring Boot, and Python services',
            'Implemented encryption, anonymization, and monitored data-distribution flows that reduced operating cost by nearly USD 15,000 per month and improved migration-team efficiency by about 70%'
        )
        Tools = @('Angular', 'TypeScript', 'AWS', 'Amazon EKS', 'Terraform', 'API Gateway', 'Salesforce Marketing Cloud', 'Amazon DocumentDB', 'Java', 'Python')
    }
    'Independent' = @{
        Company = 'Independent'
        Location = 'Cali, Colombia'
        Role = 'Software Consultant'
        Period = 'Jan 2024 - Jun 2024'
        Bullets = @(
            'Modernized the Ministry of Transport traffic fines payment platform through backend migration, API redesign, and React-based frontend delivery for a critical public service',
            'Migrated the core payment module from Java 8 to Java 21 and documented the target architecture to support future payment-processing modules',
            'Created a new REST API and administrative frontend for inquiry, payment, and user-management flows while improving Oracle 13c query performance by 40%'
        )
        Tools = @('Java 21', 'REST API', 'React', 'Oracle 13c', 'Redis', 'Secure Authentication')
    }
    'Bold' = @{
        Company = 'Bold'
        Location = 'Cali, Colombia'
        Role = 'Backend Software Engineer'
        Period = 'Aug 2023 - Dec 2023'
        Bullets = @(
            'Delivered backend services for low-balance accounts and microloans in the Bold CF application during the first Android banking-product prototype launch',
            'Built backend services and domain models in Python and Java following Domain-Driven Design across multiple microservices',
            'Supported cloud-native environments across AWS, GCP, and Azure, combining SQL and NoSQL persistence, technical documentation, and automated test design'
        )
        Tools = @('Python', 'Java', 'FastAPI', 'DDD', 'DynamoDB', 'AWS', 'Pytest')
    }
    'Banco Union' = @{
        Company = 'Banco Union'
        Location = 'Cali, Colombia'
        Role = 'Tech Lead & Senior Developer Analyst'
        Period = 'Mar 2022 - Aug 2023'
        Bullets = @(
            'Led a strategic technology and compliance program that supported the bank operating-license process and rollout of a new core banking platform',
            'Built the inter-application communication backbone for a new core banking platform, including an API Gateway and mediation layer with more than 94 REST and SOAP services',
            'Standardized more than 130 ETL jobs and reports, produced technical standards and manuals for audit readiness, and modernized delivery with AWS Glue, Azure Data Factory, Pentaho, and GoAnywhere',
            'Delivered SARLAFT risk-profile and identity-validation capabilities, improved WildFly stability, and strengthened reporting for operations and collections'
        )
        Tools = @('Java', 'Spring Boot', 'REST', 'SOAP', 'AWS Glue', 'Azure Data Factory', 'GoAnywhere', 'Power BI')
    }
    'Banco de Occidente|Senior Developer Analyst' = @{
        Company = 'Banco de Occidente'
        Location = 'Cali, Colombia'
        Role = 'Senior Developer Analyst'
        Period = 'Jan 2021 - Jan 2022'
        Bullets = @(
            'Built four internal applications in Java, Flask, and Django, improving reporting and decision-making by 30% to 40% for business units',
            'Optimized five production ETL pipelines involved in the loan portfolio closing process, reducing close time by 1.5 hours',
            'Designed and implemented the national encryption model and an anonymized replica database to strengthen customer identity protection and secure information exchange',
            'Contributed to Wompi integration, improved FLEXCUBE data processing, and served as an AWS, Terraform, and Jenkins reference for modernization initiatives'
        )
        Tools = @('Java', 'Python', 'Flask', 'Django', 'AWS', 'Terraform', 'Jenkins', 'ETL')
    }
    'Banco de Occidente|Professional Analyst' = @{
        Company = 'Banco de Occidente'
        Location = 'Cali, Colombia'
        Role = 'Professional Analyst'
        Period = 'May 2019 - Dec 2020'
        Bullets = @(
            'Designed the technology architecture for migrating an active loan portfolio product to Oracle FLEXCUBE and defined migration rules with business and technology stakeholders',
            'Built 15 IBM DataStage ETL pipelines and optimized ODS queries and reporting flows to improve data management and decision-making',
            'Led an 11-person team as Scrum Master through migration rehearsals with Oracle and internal stakeholders until the process became reliable at scale',
            'Contributed to a migration that reached 99.8% success across 39,000 customers and 80,000 loans'
        )
        Tools = @('Oracle FLEXCUBE', 'IBM DataStage', 'Java EE', 'Python', 'Jenkins', 'PostgreSQL', 'Oracle')
    }
    'Innovar Web' = @{
        Company = 'Innovar Web'
        Location = 'Cali, Colombia'
        Role = 'Data Analyst'
        Period = 'Jan 2018 - Apr 2019'
        Bullets = @(
            'Normalized the application database to Third Normal Form and materially improved system performance',
            'Built a mathematical model that predicted database latency with 90% accuracy and helped identify low-performance conditions',
            'Increased daily transactions by 30% through database optimization, query tuning, and data-layer redesign'
        )
        Tools = @('SQL', 'Database Design', 'Query Optimization', 'Performance Tuning', '3NF Modeling')
    }
}

$orderedExperiences = @()
foreach ($item in $experienceData.items) {
    $mapKey = if ($item.company -eq 'Banco de Occidente') {
        '{0}|{1}' -f $item.company, $item.role
    }
    else {
        $item.company
    }

    if (-not $experienceMap.ContainsKey($mapKey)) {
        throw "No mapped Globant CV entry found for company/role key '$mapKey'."
    }

    $orderedExperiences += $experienceMap[$mapKey]
}

$educationItem = $educationData.items[0]

Copy-Item -LiteralPath $templatePath -Destination $outputPath -Force

$word = $null
$sourceDoc = $null
$targetDoc = $null

try {
    $word = New-Object -ComObject Word.Application
    $word.Visible = $false
    $word.DisplayAlerts = 0

    $sourceDoc = $word.Documents.Open($templatePath)
    $targetDoc = $word.Documents.Open($outputPath)

    Set-FirstParagraphName -Document $targetDoc -Text $personalInfo.fullName
    Set-ParagraphText -Document $targetDoc -Index 5 -Text $summary

    for ($i = 0; $i -lt $qualifications.Count; $i++) {
        Set-ParagraphText -Document $targetDoc -Index (11 + $i) -Text $qualifications[$i]
    }

    for ($i = 0; $i -lt $languageLines.Count; $i++) {
        Set-ParagraphText -Document $targetDoc -Index (26 + $i) -Text $languageLines[$i]
    }

    Clear-LanguageParagraph -Document $targetDoc -Index 28
    Clear-LanguageParagraph -Document $targetDoc -Index 29

    $deleteRange = $targetDoc.Range($targetDoc.Paragraphs.Item(35).Range.Start, $targetDoc.Content.End - 1)
    [void]$deleteRange.Delete()

    foreach ($job in $orderedExperiences) {
        Copy-FormattedParagraphRange -SourceDocument $sourceDoc -StartIndex 58 -EndIndex 60 -TargetDocument $targetDoc
        foreach ($bullet in $job.Bullets) {
            Copy-FormattedParagraphRange -SourceDocument $sourceDoc -StartIndex 61 -EndIndex 61 -TargetDocument $targetDoc
        }
        Copy-FormattedParagraphRange -SourceDocument $sourceDoc -StartIndex 63 -EndIndex 65 -TargetDocument $targetDoc

        $bulletCount = $job.Bullets.Count
        $blockParagraphCount = $bulletCount + 6
        $blockStart = $targetDoc.Paragraphs.Count - $blockParagraphCount
        $companyLine = '{0}, {1}' -f $job.Company, $job.Location
        $toolsLine = 'Tools/Technologies: {0}' -f (($job.Tools | ForEach-Object { $_.Trim() }) -join ', ')

        Set-BoldCompanyLine -Document $targetDoc -Index $blockStart -Text $companyLine
        Set-ParagraphTextWithTab -Document $targetDoc -Index ($blockStart + 1) -LeftText $job.Role -RightText (Convert-PeriodLabel $job.Period)
        Set-ParagraphText -Document $targetDoc -Index ($blockStart + 2) -Text 'Responsibilities:'
        for ($bulletIndex = 0; $bulletIndex -lt $bulletCount; $bulletIndex++) {
            Set-ParagraphText -Document $targetDoc -Index ($blockStart + 3 + $bulletIndex) -Text (Normalize-BulletText $job.Bullets[$bulletIndex])
        }
        Set-ParagraphText -Document $targetDoc -Index ($blockStart + 4 + $bulletCount) -Text $toolsLine
    }

    Copy-FormattedParagraphRange -SourceDocument $sourceDoc -StartIndex 73 -EndIndex 77 -TargetDocument $targetDoc

    $educationStart = $targetDoc.Paragraphs.Count - 5
    Set-ParagraphText -Document $targetDoc -Index ($educationStart + 2) -Text 'Education'

    $educationLine = '{0}, {1}' -f $educationItem.institution, $educationItem.location
    Set-EducationLine -Document $targetDoc -Index ($educationStart + 3) -InstitutionLine $educationLine -Dates (Convert-PeriodLabel $educationItem.period)
    Set-ParagraphText -Document $targetDoc -Index ($educationStart + 4) -Text $educationItem.degree

    $targetDoc.Save()
}
finally {
    if ($targetDoc) {
        $targetDoc.Close([ref]$false)
    }
    if ($sourceDoc) {
        $sourceDoc.Close([ref]$false)
    }
    if ($word) {
        $word.Quit()
    }

    foreach ($comObject in @($targetDoc, $sourceDoc, $word)) {
        if ($comObject) {
            [void][System.Runtime.InteropServices.Marshal]::ReleaseComObject($comObject)
        }
    }

    [GC]::Collect()
    [GC]::WaitForPendingFinalizers()
}
