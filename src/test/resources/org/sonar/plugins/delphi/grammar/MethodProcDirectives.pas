unit MethodProcDirectives;

interface

type
  WithLeadingSemicolon = class
  private
    function FunctionOverload: Boolean; overload;
    procedure ProcedureOverload; overload;
    function FunctionOverloadedSTDCall: Integer; stdcall; overload;

    function FunctionOverloadWithoutSemicolon: Boolean overload;
    procedure ProcedureOverloadWithoutSemicolon overload;
    function FunctionOverloadedSTDCallWithoutSemicolon: Integer stdcall; overload;
  end;

  WithoutLeadingSemicolon = class
  private
    function FunctionOverloadWithoutSemicolon: Boolean overload;
    procedure ProcedureOverloadWithoutSemicolon overload;
    function FunctionOverloadedSTDCallWithoutSemicolon: Integer stdcall; overload;
  end;


  STDCallFunctionType = function(Parameter: Pointer): Integer; stdcall;
  STDCallProcedureType = procedure(Parameter: Pointer); stdcall;

  STDCallFunctionTypeWithoutSemicolon = function(Parameter: Pointer): Integer stdcall;
  STDCallProcedureTypeWithoutSemicolon = procedure(Parameter: Pointer) stdcall;

  function ExternalFunction; external 'MyCoolDLL.dll' name 'MyCoolExternalFunction';
  function ExternalFunctionWithLeadingDirectives; stdcall; external 'MyCoolDLL.dll' name 'MyCoolExternalFunction';

  function ExternalFunctionWithoutSemicolon external 'MyCoolDLL.dll' name 'MyCoolExternalFunction';
  function ExternalFunctionWithLeadingDirectivesAndNoSemicolon stdcall; external 'MyCoolDLL.dll' name 'MyCoolExternalFunction';
implementation

end.
