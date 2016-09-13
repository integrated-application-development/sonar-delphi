unit ListUtils;

interface

{$I Elotech.inc}

{$IFDEF Delphi7}
uses Classes;
{$ELSE}
{$ENDIF}
type
  TListUtils = class
  public
    class procedure Normal();
    class procedure AddAll1(AList: TList<string>; const AValue: string; ADelimiter: Char = ';');overload;
    class procedure AddAll2(AList: TList<Integer>; const AValue: string; ADelimiter: Char = ';');overload;
    class procedure AddAll3<T>(AList: TList<T>; const AValue: string; AConverter: TFunc<String,T>; ADelimiter: Char = ';');overload;
  end;

implementation

{ TListUtils }

class procedure TListUtils.AddAll1(AList: TList<Integer>; const AValue: string; ADelimiter: Char);
begin
  AddAll3<Integer>(AList, AValue, function (Value: String): Integer
                                 begin
                                   Result := StrToInt(Value);
                                 end,
                                 ADelimiter);
end;

class procedure TListUtils.AddAll2(AList: TList<string>; const AValue: string; ADelimiter: Char);
begin
  AddAll3<String>(AList, AValue, function (Value: String): string
                                 begin
                                   Result := Value;
                                 end,
                                 ADelimiter);
end;

class procedure TListUtils.AddAll3<T>(AList: TList<T>; const AValue: string; AConverter: TFunc<String,T>; ADelimiter: Char);
var
  vList: TStringList;
  vItem: String;
begin
  Assert(Assigned(AConverter), 'Converter n�o definido');

  vList := TStringList.Create;
  try
    vList.Delimiter := ADelimiter;
    vList.DelimitedText := AValue;

    for vItem in vList do
    begin
      AList.Add(AConverter(vItem))
    end;
  finally
    vList.Free;
  end;
end;

class procedure TListUtils.Normal;
begin
  //Comment
end;

end.
