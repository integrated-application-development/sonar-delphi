unit AnonymousMethods;

interface

type
  TStringPredicate = reference to function(const aValue : string) : Boolean;

function StringMatches(const aValue : string; aPredicate : TStringPredicate) : Boolean;
function IsStringEmpty(const aValue : string) : Boolean;

implementation

function StringMatches(const aValue : string; aPredicate : TStringPredicate) : Boolean;
begin
  Result := aPredicate(aValue);
end;

function IsStringEmpty(const aValue : string) : Boolean;
begin
  Result := StringMatches(aValue,
    function(const aValue : string) : Boolean
    begin
      Result := aValue = '';
    end);
end;

function IsStringEmptyImmediateInvocation(const aValue : string) : Boolean;
begin
  Result := (function(const aValue : string) : Boolean
    begin
      Result := aValue = '';
    end)(aValue);
end;

function IsStringEmptyWithCallingConvention(const aValue : string) : Boolean;
begin
  Result := StringMatches(aValue,
    function(const aValue : string) : Boolean register
    begin
      Result := aValue = '';
    end);
end;

end.
