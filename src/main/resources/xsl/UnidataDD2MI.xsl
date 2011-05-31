<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gmi="http://www.isotc211.org/2005/gmi" xmlns:gmx="http://www.isotc211.org/2005/gmx" xmlns:gsr="http://www.isotc211.org/2005/gsr" xmlns:gss="http://www.isotc211.org/2005/gss" xmlns:gts="http://www.isotc211.org/2005/gts" xmlns:gml="http://www.opengis.net/gml" xmlns:gmd2="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="gmd2 xs xsi xsl">
	<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<gmi:MI_Metadata>
			<xsl:attribute name="xsi:schemaLocation">
				<xsl:value-of select="'http://www.isotc211.org/2005/gmi C:/DOCUME~1/haber/MYDOCU~1/NOAA/Metadata/ISOSTA~1/ISO191~3/19139S~1/gmi/gmi.xsd'"/>
			</xsl:attribute>
			<xsl:variable name="var1_instance" select="."/>
			<xsl:for-each select="$var1_instance/gmd2:netcdf">
				<gmd:fileIdentifier>
					<xsl:for-each select="gmd2:attribute">
						<xsl:variable name="var4_attribute" select="."/>
						<xsl:if test="$var4_attribute/@value">
							<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
								<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
									<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
										<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
											<xsl:if test="string((string(@name) = 'id')) != 'false'">
												<gco:CharacterString>
													<xsl:value-of select="string(@value)"/>
												</gco:CharacterString>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
				</gmd:fileIdentifier>
				<gmd:contact>
					<xsl:attribute name="gco:nilReason">
						<xsl:value-of select="'unknown'"/>
					</xsl:attribute>
				</gmd:contact>
				<gmd:dateStamp>
					<xsl:attribute name="gco:nilReason">
						<xsl:value-of select="'unknown'"/>
					</xsl:attribute>
				</gmd:dateStamp>
				<gmd:spatialRepresentationInfo>
					<gmd:MD_Georectified>
						<gmd:numberOfDimensions>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:numberOfDimensions>
						<gmd:axisDimensionProperties>
							<gmd:MD_Dimension>
								<gmd:dimensionName>
									<gmd:MD_DimensionNameTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_DimensionNameTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'column'"/>
										</xsl:attribute>
										<xsl:value-of select="'column'"/>
									</gmd:MD_DimensionNameTypeCode>
								</gmd:dimensionName>
								<gmd:dimensionSize>
									<xsl:attribute name="gco:nilReason">
										<xsl:value-of select="'unknown'"/>
									</xsl:attribute>
								</gmd:dimensionSize>
								<gmd:resolution>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var6_attribute" select="."/>
										<xsl:if test="$var6_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																	<xsl:if test="string((string(@name) = 'geospatial_lon_resolution')) != 'false'">
																																		<gco:Measure>
																																			<xsl:if test="string((string(@name) = 'geospatial_lon_units')) != 'false'">
																																				<xsl:attribute name="uom">
																																					<xsl:value-of select="string(@value)"/>
																																				</xsl:attribute>
																																			</xsl:if>
																																			<xsl:value-of select="number(string(string(@value)))"/>
																																		</gco:Measure>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:resolution>
							</gmd:MD_Dimension>
						</gmd:axisDimensionProperties>
						<gmd:axisDimensionProperties>
							<gmd:MD_Dimension>
								<gmd:dimensionName>
									<gmd:MD_DimensionNameTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_DimensionNameTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'row'"/>
										</xsl:attribute>
										<xsl:value-of select="'row'"/>
									</gmd:MD_DimensionNameTypeCode>
								</gmd:dimensionName>
								<gmd:dimensionSize>
									<xsl:attribute name="gco:nilReason">
										<xsl:value-of select="'unknown'"/>
									</xsl:attribute>
								</gmd:dimensionSize>
								<gmd:resolution>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var8_attribute" select="."/>
										<xsl:if test="$var8_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																					<xsl:if test="string((string(@name) = 'geospatial_lat_resolution')) != 'false'">
																																						<gco:Measure>
																																							<xsl:if test="string((string(@name) = 'geospatial_lat_units')) != 'false'">
																																								<xsl:attribute name="uom">
																																									<xsl:value-of select="string(@value)"/>
																																								</xsl:attribute>
																																							</xsl:if>
																																							<xsl:value-of select="number(string(string(@value)))"/>
																																						</gco:Measure>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:resolution>
							</gmd:MD_Dimension>
						</gmd:axisDimensionProperties>
						<gmd:axisDimensionProperties>
							<gmd:MD_Dimension>
								<gmd:dimensionName>
									<gmd:MD_DimensionNameTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_DimensionNameTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'vertical'"/>
										</xsl:attribute>
										<xsl:value-of select="'vertical'"/>
									</gmd:MD_DimensionNameTypeCode>
								</gmd:dimensionName>
								<gmd:dimensionSize>
									<xsl:attribute name="gco:nilReason">
										<xsl:value-of select="'unknown'"/>
									</xsl:attribute>
								</gmd:dimensionSize>
								<gmd:resolution>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var10_attribute" select="."/>
										<xsl:if test="$var10_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																								<xsl:if test="string((string(@name) = 'geospatial_vertical_resolution')) != 'false'">
																																									<gco:Measure>
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																											<xsl:if test="string((string(@name) = 'geospatial_vertical_units')) != 'false'">
																																												<xsl:attribute name="uom">
																																													<xsl:value-of select="string(@value)"/>
																																												</xsl:attribute>
																																											</xsl:if>
																																										</xsl:if>
																																										<xsl:value-of select="number(string(string(@value)))"/>
																																									</gco:Measure>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:resolution>
							</gmd:MD_Dimension>
						</gmd:axisDimensionProperties>
						<gmd:cellGeometry>
							<gmd:MD_CellGeometryCode>
								<xsl:attribute name="codeList">
									<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_CellGeometryCode')"/>
								</xsl:attribute>
								<xsl:attribute name="codeListValue">
									<xsl:value-of select="'area'"/>
								</xsl:attribute>
								<xsl:value-of select="'area'"/>
							</gmd:MD_CellGeometryCode>
						</gmd:cellGeometry>
						<gmd:transformationParameterAvailability>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:transformationParameterAvailability>
						<gmd:checkPointAvailability>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:checkPointAvailability>
						<gmd:cornerPoints>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:cornerPoints>
						<gmd:pointInPixel>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:pointInPixel>
					</gmd:MD_Georectified>
				</gmd:spatialRepresentationInfo>
				<gmd:identificationInfo>
					<gmd:MD_DataIdentification>
						<gmd:citation>
							<gmd:CI_Citation>
								<gmd:title>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var12_attribute" select="."/>
										<xsl:if test="$var12_attribute/@value">
											<xsl:if test="string((string(@name) = 'title')) != 'false'">
												<xsl:variable name="var14_cond_result_equal">
													<xsl:if test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
														<xsl:value-of select="'1'"/>
													</xsl:if>
												</xsl:variable>
												<xsl:if test="string(boolean(string($var14_cond_result_equal))) != 'false'">
													<xsl:attribute name="gco:nilReason">
														<xsl:variable name="var15_cond_result_equal">
															<xsl:if test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
																<xsl:value-of select="'missing'"/>
															</xsl:if>
														</xsl:variable>
														<xsl:value-of select="string($var15_cond_result_equal)"/>
													</xsl:attribute>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var16_attribute" select="."/>
										<xsl:if test="$var16_attribute/@value">
											<xsl:if test="string((string(@name) = 'title')) != 'false'">
												<xsl:variable name="var18_cond_result_equal">
													<xsl:choose>
														<xsl:when test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
															<xsl:variable name="var20_seq_" select="/.."/>
															<xsl:if test="string(($var20_seq_) = ($var20_seq_)) != 'false'">
																<xsl:value-of select="'1'"/>
															</xsl:if>
														</xsl:when>
														<xsl:otherwise>
															<xsl:value-of select="'1'"/>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:variable>
												<xsl:if test="string(boolean(string($var18_cond_result_equal))) != 'false'">
													<gco:CharacterString>
														<xsl:variable name="var19_cond_result_equal">
															<xsl:if test="string(not((string-length(translate(string(@value), ' ', '')) = '0'))) != 'false'">
																<xsl:value-of select="string(@value)"/>
															</xsl:if>
														</xsl:variable>
														<xsl:value-of select="string($var19_cond_result_equal)"/>
													</gco:CharacterString>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:title>
								<gmd:date>
									<gmd:CI_Date>
										<gmd:date>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var21_attribute" select="."/>
												<xsl:if test="$var21_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string((string(@name) = 'date_created')) != 'false'">
																							<gco:DateTime>
																								<xsl:value-of select="string(@value)"/>
																							</gco:DateTime>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:date>
										<gmd:dateType>
											<gmd:CI_DateTypeCode>
												<xsl:attribute name="codeList">
													<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_DateTypeCode')"/>
												</xsl:attribute>
												<xsl:attribute name="codeListValue">
													<xsl:value-of select="'creation'"/>
												</xsl:attribute>
												<xsl:value-of select="'creation'"/>
											</gmd:CI_DateTypeCode>
										</gmd:dateType>
									</gmd:CI_Date>
								</gmd:date>
								<gmd:date>
									<gmd:CI_Date>
										<gmd:date>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var23_attribute" select="."/>
												<xsl:if test="$var23_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string((string(@name) = 'date_issued')) != 'false'">
																								<gco:DateTime>
																									<xsl:value-of select="string(@value)"/>
																								</gco:DateTime>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:date>
										<gmd:dateType>
											<gmd:CI_DateTypeCode>
												<xsl:attribute name="codeList">
													<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_DateTypeCode')"/>
												</xsl:attribute>
												<xsl:attribute name="codeListValue">
													<xsl:value-of select="'issued'"/>
												</xsl:attribute>
												<xsl:value-of select="'issued'"/>
											</gmd:CI_DateTypeCode>
										</gmd:dateType>
									</gmd:CI_Date>
								</gmd:date>
								<gmd:date>
									<gmd:CI_Date>
										<gmd:date>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var25_attribute" select="."/>
												<xsl:if test="$var25_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string((string(@name) = 'date_modified')) != 'false'">
																									<gco:DateTime>
																										<xsl:value-of select="string(@value)"/>
																									</gco:DateTime>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:date>
										<gmd:dateType>
											<gmd:CI_DateTypeCode>
												<xsl:attribute name="codeList">
													<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_DateTypeCode')"/>
												</xsl:attribute>
												<xsl:attribute name="codeListValue">
													<xsl:value-of select="'revision'"/>
												</xsl:attribute>
												<xsl:value-of select="'revision'"/>
											</gmd:CI_DateTypeCode>
										</gmd:dateType>
									</gmd:CI_Date>
								</gmd:date>
								<gmd:identifier>
									<gmd:MD_Identifier>
										<gmd:authority>
											<gmd:CI_Citation>
												<gmd:title>
													<xsl:attribute name="gco:nilReason">
														<xsl:value-of select="'unknown'"/>
													</xsl:attribute>
												</gmd:title>
												<gmd:date>
													<xsl:attribute name="gco:nilReason">
														<xsl:value-of select="'unknown'"/>
													</xsl:attribute>
												</gmd:date>
												<gmd:citedResponsibleParty>
													<gmd:CI_ResponsibleParty>
														<gmd:organisationName>
															<xsl:for-each select="gmd2:attribute">
																<xsl:variable name="var27_attribute" select="."/>
																<xsl:if test="$var27_attribute/@value">
																	<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																						<xsl:if test="string((string(@name) = 'naming_authority')) != 'false'">
																							<gco:CharacterString>
																								<xsl:value-of select="string(@value)"/>
																							</gco:CharacterString>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:for-each>
														</gmd:organisationName>
														<gmd:contactInfo>
															<xsl:attribute name="gco:nilReason">
																<xsl:value-of select="'unknown'"/>
															</xsl:attribute>
														</gmd:contactInfo>
														<gmd:role>
															<gmd:CI_RoleCode>
																<xsl:attribute name="codeList">
																	<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_RoleCode')"/>
																</xsl:attribute>
																<xsl:attribute name="codeListValue">
																	<xsl:value-of select="'originator'"/>
																</xsl:attribute>
																<xsl:attribute name="codeSpace">
																	<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_RoleCode')"/>
																</xsl:attribute>
															</gmd:CI_RoleCode>
														</gmd:role>
													</gmd:CI_ResponsibleParty>
												</gmd:citedResponsibleParty>
											</gmd:CI_Citation>
										</gmd:authority>
										<gmd:code>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var29_attribute" select="."/>
												<xsl:if test="$var29_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string((string(@name) = 'id')) != 'false'">
																		<gco:CharacterString>
																			<xsl:value-of select="string(@value)"/>
																		</gco:CharacterString>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:code>
									</gmd:MD_Identifier>
								</gmd:identifier>
								<gmd:citedResponsibleParty>
									<gmd:CI_ResponsibleParty>
										<gmd:individualName>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var31_attribute" select="."/>
												<xsl:if test="$var31_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string((string(@name) = 'creator_name')) != 'false'">
																										<gco:CharacterString>
																											<xsl:value-of select="string(@value)"/>
																										</gco:CharacterString>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:individualName>
										<gmd:organisationName>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var33_attribute" select="."/>
												<xsl:if test="$var33_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string((string(@name) = 'institution')) != 'false'">
																													<gco:CharacterString>
																														<xsl:value-of select="string(@value)"/>
																													</gco:CharacterString>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:organisationName>
										<gmd:contactInfo>
											<gmd:CI_Contact>
												<gmd:address>
													<gmd:CI_Address>
														<gmd:electronicMailAddress>
															<xsl:for-each select="gmd2:attribute">
																<xsl:variable name="var35_attribute" select="."/>
																<xsl:if test="$var35_attribute/@value">
																	<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																															<xsl:if test="string((string(@name) = 'creator_email')) != 'false'">
																																<gco:CharacterString>
																																	<xsl:value-of select="string(@value)"/>
																																</gco:CharacterString>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:for-each>
														</gmd:electronicMailAddress>
													</gmd:CI_Address>
												</gmd:address>
												<gmd:onlineResource>
													<gmd:CI_OnlineResource>
														<gmd:linkage>
															<xsl:for-each select="gmd2:attribute">
																<xsl:variable name="var37_attribute" select="."/>
																<xsl:if test="$var37_attribute/@value">
																	<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																														<xsl:if test="string((string(@name) = 'creator_url')) != 'false'">
																															<gmd:URL>
																																<xsl:value-of select="string(@value)"/>
																															</gmd:URL>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:for-each>
														</gmd:linkage>
													</gmd:CI_OnlineResource>
												</gmd:onlineResource>
											</gmd:CI_Contact>
										</gmd:contactInfo>
										<gmd:role>
											<gmd:CI_RoleCode>
												<xsl:attribute name="codeList">
													<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_RoleCode')"/>
												</xsl:attribute>
												<xsl:attribute name="codeListValue">
													<xsl:value-of select="'originator'"/>
												</xsl:attribute>
												<xsl:value-of select="'originator'"/>
											</gmd:CI_RoleCode>
										</gmd:role>
									</gmd:CI_ResponsibleParty>
								</gmd:citedResponsibleParty>
								<gmd:citedResponsibleParty>
									<gmd:CI_ResponsibleParty>
										<gmd:organisationName>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var39_attribute" select="."/>
												<xsl:if test="$var39_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																														<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																															<xsl:if test="string(not((string(@name) = 'license'))) != 'false'">
																																																<xsl:if test="string(not((string(@name) = 'contributor_name'))) != 'false'">
																																																	<xsl:if test="string(not((string(@name) = 'contributor_role'))) != 'false'">
																																																		<xsl:if test="string((string(@name) = 'publisher_name')) != 'false'">
																																																			<gco:CharacterString>
																																																				<xsl:value-of select="string(@value)"/>
																																																			</gco:CharacterString>
																																																		</xsl:if>
																																																	</xsl:if>
																																																</xsl:if>
																																															</xsl:if>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:organisationName>
										<gmd:contactInfo>
											<gmd:CI_Contact>
												<gmd:address>
													<gmd:CI_Address>
														<gmd:electronicMailAddress>
															<xsl:for-each select="gmd2:attribute">
																<xsl:variable name="var41_attribute" select="."/>
																<xsl:if test="$var41_attribute/@value">
																	<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																														<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																															<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																																<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																																	<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																																		<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																																			<xsl:if test="string(not((string(@name) = 'license'))) != 'false'">
																																																				<xsl:if test="string(not((string(@name) = 'contributor_name'))) != 'false'">
																																																					<xsl:if test="string(not((string(@name) = 'contributor_role'))) != 'false'">
																																																						<xsl:if test="string(not((string(@name) = 'publisher_name'))) != 'false'">
																																																							<xsl:if test="string(not((string(@name) = 'publisher_url'))) != 'false'">
																																																								<xsl:if test="string((string(@name) = 'publisher_email')) != 'false'">
																																																									<gco:CharacterString>
																																																										<xsl:value-of select="string(@value)"/>
																																																									</gco:CharacterString>
																																																								</xsl:if>
																																																							</xsl:if>
																																																						</xsl:if>
																																																					</xsl:if>
																																																				</xsl:if>
																																																			</xsl:if>
																																																		</xsl:if>
																																																	</xsl:if>
																																																</xsl:if>
																																															</xsl:if>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:for-each>
														</gmd:electronicMailAddress>
													</gmd:CI_Address>
												</gmd:address>
												<gmd:onlineResource>
													<gmd:CI_OnlineResource>
														<gmd:linkage>
															<xsl:for-each select="gmd2:attribute">
																<xsl:variable name="var43_attribute" select="."/>
																<xsl:if test="$var43_attribute/@value">
																	<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																														<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																															<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																																<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																																	<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																																		<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																																			<xsl:if test="string(not((string(@name) = 'license'))) != 'false'">
																																																				<xsl:if test="string(not((string(@name) = 'contributor_name'))) != 'false'">
																																																					<xsl:if test="string(not((string(@name) = 'contributor_role'))) != 'false'">
																																																						<xsl:if test="string(not((string(@name) = 'publisher_name'))) != 'false'">
																																																							<xsl:if test="string((string(@name) = 'publisher_url')) != 'false'">
																																																								<gmd:URL>
																																																									<xsl:value-of select="string(@value)"/>
																																																								</gmd:URL>
																																																							</xsl:if>
																																																						</xsl:if>
																																																					</xsl:if>
																																																				</xsl:if>
																																																			</xsl:if>
																																																		</xsl:if>
																																																	</xsl:if>
																																																</xsl:if>
																																															</xsl:if>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:for-each>
														</gmd:linkage>
													</gmd:CI_OnlineResource>
												</gmd:onlineResource>
											</gmd:CI_Contact>
										</gmd:contactInfo>
										<gmd:role>
											<gmd:CI_RoleCode>
												<xsl:attribute name="codeList">
													<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_RoleCode')"/>
												</xsl:attribute>
												<xsl:attribute name="codeListValue">
													<xsl:value-of select="'publisher'"/>
												</xsl:attribute>
												<xsl:value-of select="'publisher'"/>
											</gmd:CI_RoleCode>
										</gmd:role>
									</gmd:CI_ResponsibleParty>
								</gmd:citedResponsibleParty>
								<gmd:citedResponsibleParty>
									<gmd:CI_ResponsibleParty>
										<gmd:individualName>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var45_attribute" select="."/>
												<xsl:if test="$var45_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																														<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																															<xsl:if test="string(not((string(@name) = 'license'))) != 'false'">
																																																<xsl:if test="string((string(@name) = 'contributor_name')) != 'false'">
																																																	<gco:CharacterString>
																																																		<xsl:value-of select="string(@value)"/>
																																																	</gco:CharacterString>
																																																</xsl:if>
																																															</xsl:if>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:individualName>
										<gmd:role>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var47_attribute" select="."/>
												<xsl:if test="$var47_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																														<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																															<xsl:if test="string(not((string(@name) = 'license'))) != 'false'">
																																																<xsl:if test="string(not((string(@name) = 'contributor_name'))) != 'false'">
																																																	<xsl:if test="string((string(@name) = 'contributor_role')) != 'false'">
																																																		<gmd:CI_RoleCode>
																																																			<xsl:attribute name="codeList">
																																																				<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'CI_RoleCode')"/>
																																																			</xsl:attribute>
																																																			<xsl:attribute name="codeListValue">
																																																				<xsl:value-of select="string(@value)"/>
																																																			</xsl:attribute>
																																																			<xsl:value-of select="string(@value)"/>
																																																		</gmd:CI_RoleCode>
																																																	</xsl:if>
																																																</xsl:if>
																																															</xsl:if>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:role>
									</gmd:CI_ResponsibleParty>
								</gmd:citedResponsibleParty>
							</gmd:CI_Citation>
						</gmd:citation>
						<gmd:abstract>
							<xsl:for-each select="gmd2:attribute">
								<xsl:variable name="var49_attribute" select="."/>
								<xsl:if test="$var49_attribute/@value">
									<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
										<xsl:if test="string((string(@name) = 'summary')) != 'false'">
											<xsl:variable name="var51_cond_result_equal">
												<xsl:if test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
													<xsl:value-of select="'1'"/>
												</xsl:if>
											</xsl:variable>
											<xsl:if test="string(boolean(string($var51_cond_result_equal))) != 'false'">
												<xsl:attribute name="gco:nilReason">
													<xsl:variable name="var52_cond_result_equal">
														<xsl:if test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
															<xsl:value-of select="'missing'"/>
														</xsl:if>
													</xsl:variable>
													<xsl:value-of select="string($var52_cond_result_equal)"/>
												</xsl:attribute>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
							<xsl:for-each select="gmd2:attribute">
								<xsl:variable name="var53_attribute" select="."/>
								<xsl:if test="$var53_attribute/@value">
									<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
										<xsl:if test="string((string(@name) = 'summary')) != 'false'">
											<xsl:variable name="var55_cond_result_equal">
												<xsl:choose>
													<xsl:when test="string((string-length(translate(string(@value), ' ', '')) = '0')) != 'false'">
														<xsl:variable name="var57_seq_" select="/.."/>
														<xsl:if test="string(($var57_seq_) = ($var57_seq_)) != 'false'">
															<xsl:value-of select="'1'"/>
														</xsl:if>
													</xsl:when>
													<xsl:otherwise>
														<xsl:value-of select="'1'"/>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:variable>
											<xsl:if test="string(boolean(string($var55_cond_result_equal))) != 'false'">
												<gco:CharacterString>
													<xsl:variable name="var56_cond_result_equal">
														<xsl:if test="string(not((string-length(translate(string(@value), ' ', '')) = '0'))) != 'false'">
															<xsl:value-of select="string(@value)"/>
														</xsl:if>
													</xsl:variable>
													<xsl:value-of select="string($var56_cond_result_equal)"/>
												</gco:CharacterString>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
						</gmd:abstract>
						<gmd:credit>
							<xsl:for-each select="gmd2:attribute">
								<xsl:variable name="var58_attribute" select="."/>
								<xsl:if test="$var58_attribute/@value">
									<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
										<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
											<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																											<xsl:if test="string((string(@name) = 'acknowledgment')) != 'false'">
																												<gco:CharacterString>
																													<xsl:value-of select="string(@value)"/>
																												</gco:CharacterString>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
						</gmd:credit>
						<gmd:descriptiveKeywords>
							<gmd:MD_Keywords>
								<gmd:keyword>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var60_attribute" select="."/>
										<xsl:if test="$var60_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string((string(@name) = 'keywords')) != 'false'">
														<gco:CharacterString>
															<xsl:value-of select="string(@value)"/>
														</gco:CharacterString>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:keyword>
								<gmd:type>
									<gmd:MD_KeywordTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_KeywordTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'theme'"/>
										</xsl:attribute>
										<xsl:value-of select="'theme'"/>
									</gmd:MD_KeywordTypeCode>
								</gmd:type>
								<gmd:thesaurusName>
									<gmd:CI_Citation>
										<gmd:title>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var62_attribute" select="."/>
												<xsl:if test="$var62_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string((string(@name) = 'keyword_vocabulary')) != 'false'">
																	<gco:CharacterString>
																		<xsl:value-of select="string(@value)"/>
																	</gco:CharacterString>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:title>
										<gmd:date>
											<xsl:attribute name="gco:nilReason">
												<xsl:value-of select="'unknown'"/>
											</xsl:attribute>
										</gmd:date>
									</gmd:CI_Citation>
								</gmd:thesaurusName>
							</gmd:MD_Keywords>
						</gmd:descriptiveKeywords>
						<gmd:descriptiveKeywords>
							<gmd:MD_Keywords>
								<xsl:for-each select="gmd2:variable/gmd2:attribute">
									<xsl:variable name="var64_attribute" select="."/>
									<xsl:if test="$var64_attribute/@value">
										<xsl:if test="string(not((string(@name) = 'units'))) != 'false'">
											<xsl:if test="string(not((string(@name) = 'valid_min'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'valid_max'))) != 'false'">
													<xsl:if test="string((string(@name) = 'standard_name')) != 'false'">
														<gmd:keyword>
															<gco:CharacterString>
																<xsl:value-of select="string(@value)"/>
															</gco:CharacterString>
														</gmd:keyword>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:for-each>
								<gmd:type>
									<gmd:MD_KeywordTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_KeywordTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'theme'"/>
										</xsl:attribute>
										<xsl:value-of select="'theme'"/>
									</gmd:MD_KeywordTypeCode>
								</gmd:type>
								<gmd:thesaurusName>
									<gmd:CI_Citation>
										<gmd:title>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var66_attribute" select="."/>
												<xsl:if test="$var66_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																														<xsl:if test="string((string(@name) = 'standard_name_vocabulary')) != 'false'">
																																															<gco:CharacterString>
																																																<xsl:value-of select="string(@value)"/>
																																															</gco:CharacterString>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:title>
										<gmd:date>
											<xsl:attribute name="gco:nilReason">
												<xsl:value-of select="'unknown'"/>
											</xsl:attribute>
										</gmd:date>
									</gmd:CI_Citation>
								</gmd:thesaurusName>
							</gmd:MD_Keywords>
						</gmd:descriptiveKeywords>
						<gmd:resourceConstraints>
							<gmd:MD_Constraints>
								<gmd:useLimitation>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var68_attribute" select="."/>
										<xsl:if test="$var68_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'time_coverage_end'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'standard_name_vocabulary'))) != 'false'">
																																													<xsl:if test="string((string(@name) = 'license')) != 'false'">
																																														<gco:CharacterString>
																																															<xsl:value-of select="string(@value)"/>
																																														</gco:CharacterString>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:useLimitation>
							</gmd:MD_Constraints>
						</gmd:resourceConstraints>
						<gmd:aggregationInfo>
							<gmd:MD_AggregateInformation>
								<gmd:aggregateDataSetName>
									<gmd:CI_Citation>
										<gmd:title>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var70_attribute" select="."/>
												<xsl:if test="$var70_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string((string(@name) = 'project')) != 'false'">
																														<gco:CharacterString>
																															<xsl:value-of select="string(@value)"/>
																														</gco:CharacterString>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:title>
										<gmd:date/>
									</gmd:CI_Citation>
								</gmd:aggregateDataSetName>
								<gmd:associationType>
									<gmd:DS_AssociationTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'DS_AssociationTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'largerWorkCitation'"/>
										</xsl:attribute>
										<xsl:value-of select="'largerWorkCitation'"/>
									</gmd:DS_AssociationTypeCode>
								</gmd:associationType>
								<gmd:initiativeType>
									<gmd:DS_InitiativeTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'DS_InitiativeTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'project'"/>
										</xsl:attribute>
										<xsl:value-of select="'project'"/>
									</gmd:DS_InitiativeTypeCode>
								</gmd:initiativeType>
							</gmd:MD_AggregateInformation>
						</gmd:aggregationInfo>
						<gmd:aggregationInfo>
							<gmd:MD_AggregateInformation>
								<gmd:aggregateDataSetIdentifier>
									<gmd:MD_Identifier>
										<gmd:authority>
											<gmd:CI_Citation>
												<gmd:title>
													<gco:CharacterString>
														<xsl:value-of select="'Unidata Common Data Model'"/>
													</gco:CharacterString>
												</gmd:title>
												<gmd:date>
													<xsl:attribute name="gco:nilReason">
														<xsl:value-of select="'unknown'"/>
													</xsl:attribute>
												</gmd:date>
											</gmd:CI_Citation>
										</gmd:authority>
										<gmd:code>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var72_attribute" select="."/>
												<xsl:if test="$var72_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string((string(@name) = 'cdm_data_type')) != 'false'">
																				<gco:CharacterString>
																					<xsl:value-of select="string(@value)"/>
																				</gco:CharacterString>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:code>
									</gmd:MD_Identifier>
								</gmd:aggregateDataSetIdentifier>
								<gmd:associationType>
									<gmd:DS_AssociationTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'DS_AssociationTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'largerWorkCitation'"/>
										</xsl:attribute>
										<xsl:value-of select="'collection'"/>
									</gmd:DS_AssociationTypeCode>
								</gmd:associationType>
								<gmd:initiativeType>
									<gmd:DS_InitiativeTypeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'DS_InitiativeTypeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'collection'"/>
										</xsl:attribute>
										<xsl:value-of select="'collection'"/>
									</gmd:DS_InitiativeTypeCode>
								</gmd:initiativeType>
							</gmd:MD_AggregateInformation>
						</gmd:aggregationInfo>
						<gmd:spatialRepresentationType>
							<xsl:for-each select="gmd2:attribute">
								<xsl:variable name="var74_attribute" select="."/>
								<xsl:if test="$var74_attribute/@value">
									<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
										<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
											<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
															<xsl:if test="string((string(@name) = 'cdm_data_type')) != 'false'">
																<gmd:MD_SpatialRepresentationTypeCode>
																	<xsl:attribute name="codeList">
																		<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_SpatialRepresentationTypeCode')"/>
																	</xsl:attribute>
																	<xsl:attribute name="codeListValue">
																		<xsl:value-of select="string(@value)"/>
																	</xsl:attribute>
																	<xsl:value-of select="string(@value)"/>
																</gmd:MD_SpatialRepresentationTypeCode>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
						</gmd:spatialRepresentationType>
						<gmd:language>
							<gco:CharacterString>
								<xsl:value-of select="'eng'"/>
							</gco:CharacterString>
						</gmd:language>
						<gmd:extent>
							<gmd:EX_Extent>
								<xsl:attribute name="id">
									<xsl:value-of select="'boundingExtent'"/>
								</xsl:attribute>
								<gmd:geographicElement>
									<gmd:EX_GeographicBoundingBox>
										<gmd:westBoundLongitude>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var76_attribute" select="."/>
												<xsl:if test="$var76_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string((string(@name) = 'geospatial_lon_min')) != 'false'">
																																	<gco:Decimal>
																																		<xsl:value-of select="number(string(string(@value)))"/>
																																	</gco:Decimal>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:westBoundLongitude>
										<gmd:eastBoundLongitude>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var78_attribute" select="."/>
												<xsl:if test="$var78_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string((string(@name) = 'geospatial_lon_max')) != 'false'">
																																		<gco:Decimal>
																																			<xsl:value-of select="number(string(string(@value)))"/>
																																		</gco:Decimal>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:eastBoundLongitude>
										<gmd:southBoundLatitude>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var80_attribute" select="."/>
												<xsl:if test="$var80_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string((string(@name) = 'geospatial_lat_min')) != 'false'">
																																					<gco:Decimal>
																																						<xsl:value-of select="number(string(string(@value)))"/>
																																					</gco:Decimal>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:southBoundLatitude>
										<gmd:northBoundLatitude>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var82_attribute" select="."/>
												<xsl:if test="$var82_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string((string(@name) = 'geospatial_lat_max')) != 'false'">
																																						<gco:Decimal>
																																							<xsl:value-of select="number(string(string(@value)))"/>
																																						</gco:Decimal>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:northBoundLatitude>
									</gmd:EX_GeographicBoundingBox>
								</gmd:geographicElement>
								<gmd:temporalElement>
									<gmd:EX_TemporalExtent>
										<gmd:extent>
											<gml:TimePeriod>
												<xsl:attribute name="gml:id">
													<xsl:value-of select="'unknown'"/>
												</xsl:attribute>
												<xsl:for-each select="gmd2:attribute">
													<xsl:variable name="var84_attribute" select="."/>
													<xsl:if test="$var84_attribute/@value">
														<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																													<xsl:if test="string((string(@name) = 'time_coverage_start')) != 'false'">
																																														<gml:beginPosition>
																																															<xsl:value-of select="string(@value)"/>
																																														</gml:beginPosition>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:for-each>
												<xsl:for-each select="gmd2:attribute">
													<xsl:variable name="var86_attribute" select="."/>
													<xsl:if test="$var86_attribute/@value">
														<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																									<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																										<xsl:if test="string(not((string(@name) = 'geospatial_vertical_max'))) != 'false'">
																																											<xsl:if test="string(not((string(@name) = 'geospatial_vertical_resolution'))) != 'false'">
																																												<xsl:if test="string(not((string(@name) = 'geospatial_vertical_units'))) != 'false'">
																																													<xsl:if test="string(not((string(@name) = 'time_coverage_start'))) != 'false'">
																																														<xsl:if test="string((string(@name) = 'time_coverage_end')) != 'false'">
																																															<gml:endPosition>
																																																<xsl:value-of select="string(@value)"/>
																																															</gml:endPosition>
																																														</xsl:if>
																																													</xsl:if>
																																												</xsl:if>
																																											</xsl:if>
																																										</xsl:if>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:for-each>
											</gml:TimePeriod>
										</gmd:extent>
									</gmd:EX_TemporalExtent>
								</gmd:temporalElement>
								<gmd:verticalElement>
									<gmd:EX_VerticalExtent>
										<gmd:minimumValue>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var88_attribute" select="."/>
												<xsl:if test="$var88_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string((string(@name) = 'geospatial_vertical_min')) != 'false'">
																																									<gco:Real>
																																										<xsl:value-of select="number(string(string(@value)))"/>
																																									</gco:Real>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:minimumValue>
										<gmd:maximumValue>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var90_attribute" select="."/>
												<xsl:if test="$var90_attribute/@value">
													<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																		<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																			<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																				<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																					<xsl:if test="string(not((string(@name) = 'comment'))) != 'false'">
																						<xsl:if test="string(not((string(@name) = 'date_created'))) != 'false'">
																							<xsl:if test="string(not((string(@name) = 'date_issued'))) != 'false'">
																								<xsl:if test="string(not((string(@name) = 'date_modified'))) != 'false'">
																									<xsl:if test="string(not((string(@name) = 'creator_name'))) != 'false'">
																										<xsl:if test="string(not((string(@name) = 'creator_url'))) != 'false'">
																											<xsl:if test="string(not((string(@name) = 'creator_email'))) != 'false'">
																												<xsl:if test="string(not((string(@name) = 'institution'))) != 'false'">
																													<xsl:if test="string(not((string(@name) = 'project'))) != 'false'">
																														<xsl:if test="string(not((string(@name) = 'processing_level'))) != 'false'">
																															<xsl:if test="string(not((string(@name) = 'acknowledgment'))) != 'false'">
																																<xsl:if test="string(not((string(@name) = 'geospatial_lon_min'))) != 'false'">
																																	<xsl:if test="string(not((string(@name) = 'geospatial_lon_max'))) != 'false'">
																																		<xsl:if test="string(not((string(@name) = 'geospatial_lon_units'))) != 'false'">
																																			<xsl:if test="string(not((string(@name) = 'geospatial_lon_resolution'))) != 'false'">
																																				<xsl:if test="string(not((string(@name) = 'geospatial_lat_min'))) != 'false'">
																																					<xsl:if test="string(not((string(@name) = 'geospatial_lat_max'))) != 'false'">
																																						<xsl:if test="string(not((string(@name) = 'geospatial_lat_units'))) != 'false'">
																																							<xsl:if test="string(not((string(@name) = 'geospatial_lat_resolution'))) != 'false'">
																																								<xsl:if test="string(not((string(@name) = 'geospatial_vertical_min'))) != 'false'">
																																									<xsl:if test="string((string(@name) = 'geospatial_vertical_max')) != 'false'">
																																										<gco:Real>
																																											<xsl:value-of select="number(string(string(@value)))"/>
																																										</gco:Real>
																																									</xsl:if>
																																								</xsl:if>
																																							</xsl:if>
																																						</xsl:if>
																																					</xsl:if>
																																				</xsl:if>
																																			</xsl:if>
																																		</xsl:if>
																																	</xsl:if>
																																</xsl:if>
																															</xsl:if>
																														</xsl:if>
																													</xsl:if>
																												</xsl:if>
																											</xsl:if>
																										</xsl:if>
																									</xsl:if>
																								</xsl:if>
																							</xsl:if>
																						</xsl:if>
																					</xsl:if>
																				</xsl:if>
																			</xsl:if>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gmd:maximumValue>
										<gmd:verticalCRS>
											<xsl:attribute name="gco:nilReason">
												<xsl:value-of select="'unknown'"/>
											</xsl:attribute>
										</gmd:verticalCRS>
									</gmd:EX_VerticalExtent>
								</gmd:verticalElement>
							</gmd:EX_Extent>
						</gmd:extent>
						<gmd:supplementalInformation>
							<xsl:for-each select="gmd2:attribute">
								<xsl:variable name="var92_attribute" select="."/>
								<xsl:if test="$var92_attribute/@value">
									<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
										<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
											<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'history'))) != 'false'">
																	<xsl:if test="string((string(@name) = 'comment')) != 'false'">
																		<gco:CharacterString>
																			<xsl:value-of select="string(@value)"/>
																		</gco:CharacterString>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:if>
								</xsl:if>
							</xsl:for-each>
						</gmd:supplementalInformation>
					</gmd:MD_DataIdentification>
				</gmd:identificationInfo>
				<gmd:contentInfo>
					<gmd:MD_CoverageDescription>
						<gmd:attributeDescription>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:attributeDescription>
						<gmd:contentType>
							<xsl:attribute name="gco:nilReason">
								<xsl:value-of select="'unknown'"/>
							</xsl:attribute>
						</gmd:contentType>
						<xsl:for-each select="gmd2:variable">
							<gmd:dimension>
								<gmi:MI_Band>
									<gmd:sequenceIdentifier>
										<gco:MemberName>
											<gco:aName>
												<gco:CharacterString>
													<xsl:value-of select="string(@name)"/>
												</gco:CharacterString>
											</gco:aName>
										</gco:MemberName>
									</gmd:sequenceIdentifier>
									<gmd:maxValue>
										<xsl:for-each select="gmd2:attribute">
											<xsl:variable name="var96_attribute" select="."/>
											<xsl:if test="$var96_attribute/@value">
												<xsl:if test="string(not((string(@name) = 'units'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'valid_min'))) != 'false'">
														<xsl:if test="string((string(@name) = 'valid_max')) != 'false'">
															<gco:Real>
																<xsl:value-of select="number(string(@value))"/>
															</gco:Real>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:for-each>
									</gmd:maxValue>
									<gmd:minValue>
										<xsl:for-each select="gmd2:attribute">
											<xsl:variable name="var98_attribute" select="."/>
											<xsl:if test="$var98_attribute/@value">
												<xsl:if test="string(not((string(@name) = 'units'))) != 'false'">
													<xsl:if test="string((string(@name) = 'valid_min')) != 'false'">
														<gco:Real>
															<xsl:value-of select="number(string(@value))"/>
														</gco:Real>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:for-each>
									</gmd:minValue>
									<gmd:units>
										<gml:UnitDefinition>
											<xsl:for-each select="gmd2:attribute">
												<xsl:variable name="var100_attribute" select="."/>
												<xsl:if test="$var100_attribute/@value">
													<xsl:if test="string((string(@name) = 'units')) != 'false'">
														<gml:description>
															<xsl:value-of select="string(@value)"/>
														</gml:description>
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</gml:UnitDefinition>
									</gmd:units>
								</gmi:MI_Band>
							</gmd:dimension>
						</xsl:for-each>
					</gmd:MD_CoverageDescription>
				</gmd:contentInfo>
				<gmd:dataQualityInfo>
					<gmd:DQ_DataQuality>
						<gmd:scope>
							<gmd:DQ_Scope>
								<gmd:level>
									<gmd:MD_ScopeCode>
										<xsl:attribute name="codeList">
											<xsl:value-of select="concat(concat('http://www.isotc211.org/2005/resources/codeList.xml', '#'), 'MD_ScopeCode')"/>
										</xsl:attribute>
										<xsl:attribute name="codeListValue">
											<xsl:value-of select="'dataset'"/>
										</xsl:attribute>
										<xsl:value-of select="'dataset'"/>
									</gmd:MD_ScopeCode>
								</gmd:level>
							</gmd:DQ_Scope>
						</gmd:scope>
						<gmd:lineage>
							<gmd:LI_Lineage>
								<gmd:statement>
									<xsl:for-each select="gmd2:attribute">
										<xsl:variable name="var102_attribute" select="."/>
										<xsl:if test="$var102_attribute/@value">
											<xsl:if test="string(not((string(@name) = 'title'))) != 'false'">
												<xsl:if test="string(not((string(@name) = 'summary'))) != 'false'">
													<xsl:if test="string(not((string(@name) = 'keywords'))) != 'false'">
														<xsl:if test="string(not((string(@name) = 'keyword_vocabulary'))) != 'false'">
															<xsl:if test="string(not((string(@name) = 'id'))) != 'false'">
																<xsl:if test="string(not((string(@name) = 'naming_authority'))) != 'false'">
																	<xsl:if test="string(not((string(@name) = 'cdm_data_type'))) != 'false'">
																		<xsl:if test="string((string(@name) = 'history')) != 'false'">
																			<gco:CharacterString>
																				<xsl:value-of select="string(@value)"/>
																			</gco:CharacterString>
																		</xsl:if>
																	</xsl:if>
																</xsl:if>
															</xsl:if>
														</xsl:if>
													</xsl:if>
												</xsl:if>
											</xsl:if>
										</xsl:if>
									</xsl:for-each>
								</gmd:statement>
							</gmd:LI_Lineage>
						</gmd:lineage>
					</gmd:DQ_DataQuality>
				</gmd:dataQualityInfo>
			</xsl:for-each>
		</gmi:MI_Metadata>
	</xsl:template>
</xsl:stylesheet>
